package billingandcalculation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.util.List;

public class BillingFrame extends JFrame {

    private final BillingModule billing = new BillingModule();

    private final DefaultTableModel productModel =
        new DefaultTableModel(
            new String[]{"ID", "Product", "Price", "Stock", "GST %"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

    private final DefaultTableModel cartModel =
        new DefaultTableModel(
            new String[]{"ID", "Product", "Qty", "Price", "Subtotal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

    private final JTable productTable = new JTable(productModel);
    private final JTable cartTable = new JTable(cartModel);

    private final JTextField productIdField = new JTextField(10);
    private final JTextField quantityField = new JTextField("1", 10);
    private final JTextField discountField = new JTextField("0", 8);

    private final JLabel subtotalLabel = new JLabel("₹0.00");
    private final JLabel gstLabel = new JLabel("₹0.00");
    private final JLabel discountLabel = new JLabel("₹0.00");
    private final JLabel totalLabel = new JLabel("₹0.00");

    public BillingFrame() {
        setTitle("Supermarket Billing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        createUI();
        refreshProducts();
        refreshCart();
    }

    private void createUI() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        main.setBackground(new Color(242, 245, 250));

        JLabel heading = new JLabel("SUPERMARKET BILLING SYSTEM");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(new Color(35, 48, 92));
        heading.setBorder(BorderFactory.createEmptyBorder(5, 0, 12, 0));
        main.add(heading, BorderLayout.NORTH);

        productTable.setRowHeight(26);
        cartTable.setRowHeight(26);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel productsPanel = new JPanel(new BorderLayout(5, 8));
        productsPanel.setBorder(BorderFactory.createTitledBorder(
            "Available Products"
        ));
        productsPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel cartPanel = new JPanel(new BorderLayout(5, 8));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Live Cart"));
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel tables = new JPanel(new GridLayout(1, 2, 12, 0));
        tables.setOpaque(false);
        tables.add(productsPanel);
        tables.add(cartPanel);

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        inputs.setBorder(BorderFactory.createTitledBorder("Billing Controls"));

        inputs.add(new JLabel("Product ID:"));
        inputs.add(productIdField);
        inputs.add(new JLabel("Quantity:"));
        inputs.add(quantityField);

        JButton addButton = new JButton("Add to Cart");
        JButton updateButton = new JButton("Update Quantity");
        JButton removeButton = new JButton("Remove Item");
        JButton clearButton = new JButton("Clear Cart");

        inputs.add(addButton);
        inputs.add(updateButton);
        inputs.add(removeButton);
        inputs.add(clearButton);

        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        removeButton.addActionListener(e -> removeItem());
        clearButton.addActionListener(e -> clearCart());

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = productTable.getSelectedRow();

                if (row >= 0) {
                    productIdField.setText(
                        productModel.getValueAt(row, 0).toString()
                    );
                }
            }
        });

        cartTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = cartTable.getSelectedRow();

                if (row >= 0) {
                    productIdField.setText(
                        cartModel.getValueAt(row, 0).toString()
                    );
                    quantityField.setText(
                        cartModel.getValueAt(row, 2).toString()
                    );
                }
            }
        });

        JPanel calculationPanel = new JPanel(new GridLayout(2, 4, 12, 8));
        calculationPanel.setBorder(BorderFactory.createTitledBorder(
            "Calculation Module"
        ));

        calculationPanel.add(new JLabel("Subtotal:"));
        calculationPanel.add(subtotalLabel);
        calculationPanel.add(new JLabel("GST:"));
        calculationPanel.add(gstLabel);
        calculationPanel.add(new JLabel("Discount (%):"));
        calculationPanel.add(discountField);
        calculationPanel.add(new JLabel("Discount Amount:"));
        calculationPanel.add(discountLabel);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        bottom.add(inputs, BorderLayout.NORTH);
        bottom.add(calculationPanel, BorderLayout.CENTER);

        JPanel checkoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        checkoutPanel.setOpaque(false);

        JLabel totalTitle = new JLabel("FINAL TOTAL: ");
        totalTitle.setFont(new Font("Arial", Font.BOLD, 19));

        totalLabel.setFont(new Font("Arial", Font.BOLD, 22));
        totalLabel.setForeground(new Color(20, 120, 75));

        JButton calculateButton = new JButton("Calculate Bill");
        JButton checkoutButton = new JButton("Complete Sale");

        calculateButton.addActionListener(e -> calculateBill());
        checkoutButton.addActionListener(e -> completeSale());

        checkoutPanel.add(calculateButton);
        checkoutPanel.add(totalTitle);
        checkoutPanel.add(totalLabel);
        checkoutPanel.add(checkoutButton);

        bottom.add(checkoutPanel, BorderLayout.SOUTH);

        main.add(tables, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void refreshProducts() {
        productModel.setRowCount(0);

        for (Product product : billing.getProducts()) {
            productModel.addRow(new Object[]{
                product.getProductId(),
                product.getName(),
                String.format("%.2f", product.getPrice()),
                product.getStock(),
                product.getGstRate()
            });
        }
    }

    private void refreshCart() {
        cartModel.setRowCount(0);

        List<CartItem> items = billing.getCartItems();

        for (CartItem item : items) {
            Product product = item.getProduct();

            cartModel.addRow(new Object[]{
                product.getProductId(),
                product.getName(),
                item.getQuantity(),
                String.format("%.2f", product.getPrice()),
                String.format("%.2f", item.getSubtotal())
            });
        }

        calculateBill();
    }

    private void addItem() {
        try {
            int id = Integer.parseInt(productIdField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());

            billing.addToCart(id, quantity);
            refreshCart();

        } catch (NumberFormatException ex) {
            showError("Enter a valid product ID and quantity.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateItem() {
        try {
            int id = Integer.parseInt(productIdField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());

            billing.updateQuantity(id, quantity);
            refreshCart();

        } catch (NumberFormatException ex) {
            showError("Enter a valid product ID and quantity.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeItem() {
        int row = cartTable.getSelectedRow();

        try {
            int id;

            if (row >= 0) {
                id = Integer.parseInt(
                    cartModel.getValueAt(row, 0).toString()
                );
            } else {
                id = Integer.parseInt(productIdField.getText().trim());
            }

            billing.removeFromCart(id);
            refreshCart();

        } catch (NumberFormatException ex) {
            showError("Select a cart item or enter its product ID.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearCart() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Clear all items from the cart?",
            "Clear Cart",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            billing.clearCart();
            refreshCart();
        }
    }

    private double readDiscountRate() {
        double rate = Double.parseDouble(discountField.getText().trim());

        if (rate < 0 || rate > 100) {
            throw new IllegalArgumentException(
                "Discount must be between 0 and 100."
            );
        }

        return rate;
    }

    private void calculateBill() {
        try {
            double rate = readDiscountRate();
            double subtotal = billing.getSubtotal();
            double gst = billing.getGST();
            double discount = billing.getDiscount(rate);
            double total = billing.getFinalTotal(rate);

            subtotalLabel.setText(String.format("₹%.2f", subtotal));
            gstLabel.setText(String.format("₹%.2f", gst));
            discountLabel.setText(String.format("₹%.2f", discount));
            totalLabel.setText(String.format("₹%.2f", total));

        } catch (NumberFormatException ex) {
            showError("Enter a valid discount percentage.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void completeSale() {
        if (billing.getCartItems().isEmpty()) {
            showError("Add at least one product before checkout.");
            return;
        }

        double rate;

        try {
    rate = readDiscountRate();
} catch (IllegalArgumentException ex) {
    showError("Enter a valid discount between 0 and 100.");
    return;
}

calculateBill();

        calculateBill();

        String receipt = buildReceipt(rate);

        int choice = JOptionPane.showConfirmDialog(
            this,
            new JScrollPane(new JTextArea(receipt, 20, 45)),
            "Confirm Sale",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            billing.completeSale();
            refreshProducts();
            refreshCart();

            JTextArea receiptArea = new JTextArea(receipt);
            receiptArea.setEditable(false);
            receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            int printChoice = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(receiptArea),
                "Sale Completed",
                JOptionPane.YES_NO_OPTION
            );

            if (printChoice == JOptionPane.YES_OPTION) {
                try {
                    receiptArea.print();
                } catch (PrinterException ex) {
                    showError("Printing failed: " + ex.getMessage());
                }
            }

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private String buildReceipt(double rate) {
        StringBuilder receipt = new StringBuilder();

        receipt.append("       SUPERMARKET BILL\n");
        receipt.append("====================================\n");
        receipt.append(String.format("%-15s %3s %9s%n",
                "Product", "Qty", "Amount"));
        receipt.append("------------------------------------\n");

        for (CartItem item : billing.getCartItems()) {
            receipt.append(String.format(
                "%-15s %3d %9.2f%n",
                item.getProduct().getName(),
                item.getQuantity(),
                item.getSubtotal()
            ));
        }

        double subtotal = billing.getSubtotal();
        double gst = billing.getGST();
        double discount = billing.getDiscount(rate);
        double total = billing.getFinalTotal(rate);

        receipt.append("------------------------------------\n");
        receipt.append(String.format("Subtotal:       Rs. %10.2f%n", subtotal));
        receipt.append(String.format("GST:            Rs. %10.2f%n", gst));
        receipt.append(String.format("Discount:       Rs. %10.2f%n", discount));
        receipt.append(String.format("FINAL TOTAL:    Rs. %10.2f%n", total));
        receipt.append("====================================\n");
        receipt.append("       Thank you for shopping!\n");

        return receipt.toString();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this, message, "Billing System",
            JOptionPane.WARNING_MESSAGE
        );
    }
}
