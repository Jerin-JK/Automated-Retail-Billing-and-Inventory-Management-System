package billing.ui;

import java.awt.*;
import java.math.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import billing.model.CartItem;
import inventory.model.Product;
import shared.model.User;
import billing.service.SalesService;
import inventory.service.InventoryService;
import shared.ui.Ui;

/** Provides product lookup, cart editing, payment entry, and sale completion. */
public class BillingPanel extends JPanel {
    // Current cashier, inventory and sales services, cart lines, and checkout controls.
    private final User cashier;
    private final InventoryService inventory = new InventoryService();
    private final SalesService sales = new SalesService();
    private final java.util.List<CartItem> cart = new ArrayList<>();
    private final JTextField lookup = new JTextField(24), quantity = new JTextField("1", 5),
            discount = new JTextField("0.00", 8), paid = new JTextField(10);
    private final JComboBox<String> method = new JComboBox<>(new String[] { "CASH", "CARD", "UPI", "OTHER" });
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "SKU", "Product", "Unit price", "Tax %", "Qty", "Line total" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JLabel totals = new JLabel("Subtotal 0.00   Tax 0.00   Total 0.00");

    /** Builds the billing screen and connects its cart and checkout actions. */
    public BillingPanel(User user) {
        super(new BorderLayout(8, 8));
        cashier = user;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Build the product lookup and quantity controls at the top of the screen.
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Scan / search product:"));
        top.add(lookup);
        top.add(new JLabel("Qty"));
        top.add(quantity);
        JButton add = new JButton("Add to cart");
        top.add(add);
        add(top, BorderLayout.NORTH);
        // Show each cart line and its calculated amount in a read-only table.
        JTable table = new JTable(model);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);
        // Place the totals, discount, payment method, and tender controls below the cart.
        JPanel bottom = new JPanel(new GridLayout(2, 1, 6, 6));
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton remove = new JButton("Remove selected line");
        row1.add(remove);
        row1.add(totals);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(new JLabel("Discount"));
        row2.add(discount);
        row2.add(new JLabel("Payment"));
        row2.add(method);
        row2.add(new JLabel("Received"));
        row2.add(paid);
        JButton checkout = new JButton("Complete sale & print receipt");
        row2.add(checkout);
        bottom.add(row1);
        bottom.add(row2);
        add(bottom, BorderLayout.SOUTH);
        // Connect search, remove, discount, and checkout controls to their actions.
        add.addActionListener(e -> addProduct());
        lookup.addActionListener(e -> addProduct());
        remove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                cart.remove(table.convertRowIndexToModel(row));
                render();
            }
        });
        discount.addActionListener(e -> render());
        checkout.addActionListener(e -> checkout(checkout));
    }

    /** Finds a product, validates its quantity, and adds or merges its cart line. */
    private void addProduct() {
        String term = lookup.getText().trim();
        if (term.isEmpty())
            return;
        try {
            // Search active inventory by the scanned code or typed product text.
            java.util.List<Product> results = inventory.search(term);
            // Prefer an exact barcode or SKU match; use the sole result when unambiguous.
            Product p = results.stream().filter(x -> term.equalsIgnoreCase(x.barcode) || term.equalsIgnoreCase(x.sku))
                    .findFirst().orElse(results.size() == 1 ? results.get(0) : null);
            if (p == null) {
                if (results.isEmpty()) {
                    Ui.info(this, "No active matching product found.");
                    return;
                }
                Object choice = JOptionPane.showInputDialog(this, "Choose a product", "Product search",
                        JOptionPane.PLAIN_MESSAGE, null,
                        results.stream().map(x -> x.sku + " — " + x.name + " (stock " + x.stock + ")").toArray(), null);
                if (choice == null)
                    return;
                int idx = java.util.Arrays.asList(
                        results.stream().map(x -> x.sku + " — " + x.name + " (stock " + x.stock + ")").toArray())
                        .indexOf(choice);
                p = results.get(idx);
            }
            // Reject zero or negative quantities before changing the cart.
            BigDecimal qty = new BigDecimal(quantity.getText());
            if (qty.signum() <= 0)
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            Product chosen = p;
            // Merge the quantity into an existing line for this product when present.
            CartItem line = cart.stream().filter(x -> x.product.id == chosen.id).findFirst().orElse(null);
            if (line == null)
                cart.add(new CartItem(p, qty));
            else
                line.quantity = line.quantity.add(qty);
            lookup.setText("");
            quantity.setText("1");
            render();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    /** Reads the entered discount and treats invalid text as zero for display. */
    private BigDecimal discountValue() {
        try {
            return new BigDecimal(discount.getText().isBlank() ? "0" : discount.getText());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /** Rebuilds the cart table and recalculates subtotal, tax, and total. */
    private void render() {
        model.setRowCount(0);
        BigDecimal subtotal = BigDecimal.ZERO, tax = BigDecimal.ZERO;
        for (CartItem line : cart) {
            BigDecimal base = line.product.sellingPrice.multiply(line.quantity);
            BigDecimal t = base.multiply(line.product.taxPercent).divide(new BigDecimal("100"), 2,
                    RoundingMode.HALF_UP);
            subtotal = subtotal.add(base);
            tax = tax.add(t);
            model.addRow(new Object[] { line.product.sku, line.product.name, line.product.sellingPrice,
                    line.product.taxPercent, line.quantity, base.add(t).setScale(2, RoundingMode.HALF_UP) });
        }
        BigDecimal d = discountValue();
        totals.setText(String.format("Subtotal %s   Tax %s   Total %s", money(subtotal), money(tax),
                money(subtotal.add(tax).subtract(d))));
    }

    /** Formats a monetary value with two decimal places. */
    private String money(BigDecimal n) {
        return n.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Validates payment, records the sale asynchronously, and offers receipt printing. */
    private void checkout(JButton button) {
        if (cart.isEmpty()) {
            Ui.info(this, "Add at least one item to the cart.");
            return;
        }
        try {
            // Calculate the amounts that will be submitted and shown on the receipt.
            BigDecimal d = new BigDecimal(discount.getText().isBlank() ? "0" : discount.getText());
            BigDecimal sub = cart.stream().map(i -> i.lineTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tax = cart.stream().map(i -> i.lineTotal().multiply(i.product.taxPercent)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal total = sub.add(tax).subtract(d);
            BigDecimal tender = new BigDecimal(paid.getText());
            if (d.signum() < 0 || d.compareTo(sub) > 0 || tender.compareTo(total) < 0)
                throw new IllegalArgumentException("Check discount and payment received.");
            // Snapshot the cart and disable repeat checkout while the database request runs.
            button.setEnabled(false);
            java.util.List<CartItem> captured = new ArrayList<>();
            for (CartItem i : cart)
                captured.add(new CartItem(i.product, i.quantity));
            // Run the transactional checkout without blocking Swing's event thread.
            new SwingWorker<String, Void>() {
                /** Sends the captured sale details to the sales service in the background. */
                protected String doInBackground() throws Exception {
                    return sales.checkout(cashier, captured, (String) method.getSelectedItem(), d, tender);
                }

                /** Prints or displays the result, clears the completed cart, and reports errors. */
                protected void done() {
                    button.setEnabled(true);
                    try {
                        String invoice = get();
                        BigDecimal change = tender.subtract(total);
                        String receipt = "SUPERMARKET\nInvoice: " + invoice + "\nCashier: " + cashier.getName() + "\n\n"
                                + receiptLines(captured) + "\nSubtotal: " + money(sub) + "\nTax: " + money(tax)
                                + "\nDiscount: " + money(d) + "\nTOTAL: " + money(total) + "\nPaid: " + money(tender)
                                + "\nChange: " + money(change) + "\nThank you!";
                        JTextArea area = new JTextArea(receipt, 24, 44);
                        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                        if (JOptionPane.showConfirmDialog(BillingPanel.this, new JScrollPane(area),
                                "Sale completed — " + invoice, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
                            area.print();
                        cart.clear();
                        discount.setText("0.00");
                        paid.setText("");
                        render();
                    } catch (Exception ex) {
                        Ui.error(BillingPanel.this, ex.getCause() == null ? ex : ex.getCause());
                    }
                }
            }.execute();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    /** Formats each captured cart line for the printable receipt. */
    private String receiptLines(java.util.List<CartItem> lines) {
        StringBuilder s = new StringBuilder();
        for (CartItem i : lines)
            s.append(i.product.name).append(" x ").append(i.quantity).append(" @ ")
                    .append(money(i.product.sellingPrice)).append(" = ").append(money(i.lineTotal())).append('\n');
        return s.toString();
    }
}
