package suppliers.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import shared.model.User;
import suppliers.service.SupplierService;
import shared.ui.Ui;

/** Displays supplier records and administrator controls for adding or disabling them. */
public class SuppliersPanel extends JPanel {
    // Administrator identity, supplier service, table, and table model.
    private final User admin;
    private final SupplierService service = new SupplierService();
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "ID", "Supplier", "Phone", "Email", "Address", "Status" }, 0) {
        /** Keep supplier table cells read-only; changes use explicit controls. */
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    /** Builds the supplier table and its management buttons. */
    public SuppliersPanel(User user) {
        super(new BorderLayout(8, 8));
        admin = user;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        table.setModel(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add supplier"), toggle = new JButton("Enable / disable"),
                reload = new JButton("Refresh");
        p.add(add);
        p.add(toggle);
        p.add(reload);
        add(p, BorderLayout.SOUTH);
        add.addActionListener(e -> addSupplier());
        toggle.addActionListener(e -> toggle());
        reload.addActionListener(e -> load());
        load();
    }

    /** Refreshes the supplier table from the service. */
    private void load() {
        try {
            model.setRowCount(0);
            for (String[] r : service.list(admin))
                model.addRow(r);
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }

    /** Collects supplier contact fields and submits a new supplier. */
    private void addSupplier() {
        // Create one input field for each supplier contact value.
        JTextField[] f = { new JTextField(), new JTextField(), new JTextField(), new JTextField() };
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        String[] labels = { "Name", "Phone", "Email", "Address" };
        for (int i = 0; i < f.length; i++) {
            form.add(new JLabel(labels[i]));
            form.add(f[i]);
        }
        if (JOptionPane.showConfirmDialog(this, form, "New supplier",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            service.add(admin, f[0].getText(), f[1].getText(), f[2].getText(), f[3].getText());
            load();
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }

    /** Changes the selected supplier between active and disabled states. */
    private void toggle() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Ui.info(this, "Select a supplier first.");
            return;
        }
        try {
            int m = table.convertRowIndexToModel(row);
            long id = Long.parseLong(model.getValueAt(m, 0).toString());
            service.setActive(admin, id, "Disabled".equals(model.getValueAt(m, 5)));
            load();
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }
}
