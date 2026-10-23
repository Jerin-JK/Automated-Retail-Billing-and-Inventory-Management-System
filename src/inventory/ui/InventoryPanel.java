package inventory.ui;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import inventory.model.Product;
import shared.model.User;
import inventory.service.InventoryService;
import shared.ui.Ui;

/** Displays inventory search results and administrator product actions. */
public class InventoryPanel extends JPanel {
    // Signed-in user, inventory service, search controls, and product table state.
    private final User user;
    private final InventoryService service = new InventoryService();
    private final JTextField search = new JTextField(24);
    private final DefaultTableModel model;
    private JTable table;
    private JButton add;
    private final String[] columns = { "ID", "SKU", "Barcode", "Product", "Category", "Unit", "Buy", "Sell", "Tax %",
            "Stock", "Reorder", "Status", "Actions" };

    /** Builds the inventory table and enables management controls for administrators. */
    public InventoryPanel(User user) {
        super(new BorderLayout(8, 8));
        this.user = user;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        model = new DefaultTableModel(columns, 0) {
            /** Only administrators can activate the action column. */
            public boolean isCellEditable(int r, int c) {
                return user.isAdmin() && c == 12;
            }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(25);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search name / SKU / barcode:"));
        top.add(search);
        JButton go = new JButton("Search");
        top.add(go);
        add = new JButton("Add product");
        if (user.isAdmin())
            top.add(add);
        JButton refresh = new JButton("Refresh");
        top.add(refresh);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        // Install the manage button only when the signed-in user is an administrator.
        if (user.isAdmin()) {
            TableColumn action = table.getColumnModel().getColumn(12);
            action.setPreferredWidth(125);
            action.setCellRenderer(new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int row,
                        int col) {
                    return new JButton("Manage");
                }
            });
            action.setCellEditor(new ActionEditor());
        }
        // Connect the search, refresh, and add-product controls to their handlers.
        go.addActionListener(e -> load());
        refresh.addActionListener(e -> {
            search.setText("");
            load();
        });
        search.addActionListener(e -> load());
        add.addActionListener(e -> edit(null));
        load();
    }

    /** Loads the selected table row as a product model. */
    private Product productAt(int row) {
        try {
            long id = Long.parseLong(model.getValueAt(row, 0).toString());
            return service.find(id);
        } catch (Exception e) {
            Ui.error(this, e);
            return null;
        }
    }

    /** Fetches matching products in the background and refreshes the table. */
    private void load() {
        new SwingWorker<List<Product>, Void>() {
            /** Queries inventory away from the Swing event thread. */
            protected List<Product> doInBackground() throws Exception {
                return service.search(search.getText());
            }

            /** Updates rows and stock status after the background search finishes. */
            protected void done() {
                try {
                    model.setRowCount(0);
                    for (Product p : get()) {
                        String status = p.stock.compareTo(p.reorderLevel) <= 0 ? "LOW STOCK" : "OK";
                        model.addRow(new Object[] { p.id, p.sku, p.barcode, p.name, p.category, p.unit, p.purchasePrice,
                                p.sellingPrice, p.taxPercent, p.stock, p.reorderLevel, status,
                                user.isAdmin() ? "Manage" : "" });
                    }
                } catch (Exception e) {
                    Ui.error(InventoryPanel.this, e.getCause() == null ? e : e.getCause());
                }
            }
        }.execute();
    }

    /** Shows the product form for a new product or the selected product. */
    private void edit(Product old) {
        JTextField[] f = new JTextField[10];
        String[] labels = { "SKU", "Barcode", "Name", "Category", "Unit", "Purchase price", "Selling price", "Tax %",
                "Stock quantity", "Reorder level" };
        // Use blank/default values for a new product and current values for an edit.
        String[] values = old == null ? new String[] { "", "", "", "", "pcs", "0", "0", "0", "0", "0" }
                : new String[] { old.sku, old.barcode, old.name, old.category, old.unit,
                        old.purchasePrice.toPlainString(), old.sellingPrice.toPlainString(),
                        old.taxPercent.toPlainString(), old.stock.toPlainString(), old.reorderLevel.toPlainString() };
        JPanel form = new JPanel(new GridLayout(labels.length, 2, 5, 5));
        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            f[i] = new JTextField(values[i]);
            form.add(f[i]);
        }
        if (JOptionPane.showConfirmDialog(this, form, old == null ? "Add product" : "Edit product",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            Product p = new Product(old == null ? 0 : old.id, f[0].getText().trim(), f[1].getText().trim(),
                    f[2].getText().trim(), f[3].getText().trim(), f[4].getText().trim(), new BigDecimal(f[5].getText()),
                    new BigDecimal(f[6].getText()), new BigDecimal(f[7].getText()), new BigDecimal(f[8].getText()),
                    new BigDecimal(f[9].getText()));
            service.save(user, p);
            load();
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }

    /** Attempts deletion and offers deactivation when history prevents removal. */
    private void delete(long id, String name) {
        if (JOptionPane.showConfirmDialog(this, "Delete '" + name + "'? This cannot be undone.", "Confirm delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            service.delete(user, id);
            Ui.info(this, "Product deleted.");
            load();
        } catch (Exception ex) {
            // If historical records block deletion, offer the reversible deactivation path.
            int choice = JOptionPane.showConfirmDialog(this,
                    "This product is referenced by past bills or stock history and cannot be deleted. Deactivate it instead?",
                    "Product is in use", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION)
                try {
                    service.deactivate(user, id);
                    load();
                } catch (Exception e) {
                    Ui.error(this, e);
                }
            else
                Ui.error(this, ex);
        }
    }

    /** Collects a stock change and reason, then refreshes inventory. */
    private void adjust(Product p) {
        String value = JOptionPane.showInputDialog(this, "Quantity change (+ to receive, - to correct):", "0");
        if (value == null)
            return;
        String reason = JOptionPane.showInputDialog(this, "Reason for stock adjustment:");
        if (reason == null)
            return;
        try {
            service.adjust(user, p.id, new BigDecimal(value), reason);
            load();
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }

    /** Turns the table action cell into a button for edit, adjustment, or deletion. */
    private final class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton("Manage");
        private int viewRow;

        /** Connects the manage button to the selected product actions. */
        ActionEditor() {
            button.addActionListener(e -> {
                fireEditingStopped();
                int row = table.convertRowIndexToModel(viewRow);
                long id = Long.parseLong(model.getValueAt(row, 0).toString());
                Product p = productAt(row);
                if (p == null)
                    return;
                // Offer the available actions for the selected product.
                String[] options = { "Edit", "Adjust stock", "Delete", "Cancel" };
                int pick = JOptionPane.showOptionDialog(InventoryPanel.this, "Choose an action for " + p.name,
                        "Product actions", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
                        options[0]);
                if (pick == 0)
                    edit(p);
                else if (pick == 1)
                    adjust(p);
                else if (pick == 2)
                    delete(id, p.name);
            });
        }

        /** Returns the label displayed by the action cell. */
        public Object getCellEditorValue() {
            return "Manage";
        }

        /** Remember which view row was clicked and return the manage button. */
        public Component getTableCellEditorComponent(JTable t, Object value, boolean selected, int row, int column) {
            viewRow = row;
            return button;
        }
    }
}
