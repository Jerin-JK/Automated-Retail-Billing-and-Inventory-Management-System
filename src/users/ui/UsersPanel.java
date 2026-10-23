package users.ui;

import auth.dao.UserDao;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import shared.model.User;
import auth.service.AuthService;
import shared.ui.Ui;

/** Displays user accounts and administrator controls for creation and status changes. */
public class UsersPanel extends JPanel {
    // Administrator identity, user data access, and account table state.
    private final User admin;
    private final UserDao dao = new UserDao();
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "ID", "Name", "Username", "Role", "Status" }, 0) {
        /** Keep account rows read-only; edits use the explicit account actions. */
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    /** Builds the users table and connects account management controls. */
    public UsersPanel(User user) {
        super(new BorderLayout(8, 8));
        admin = user;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        table.setModel(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton create = new JButton("Create user");
        JButton toggle = new JButton("Enable / disable selected");
        JButton refresh = new JButton("Refresh");
        bottom.add(create);
        bottom.add(toggle);
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);
        create.addActionListener(e -> create());
        toggle.addActionListener(e -> toggle());
        refresh.addActionListener(e -> load());
        load();
    }

    /** Reloads account rows from the user data layer. */
    private void load() {
        try {
            model.setRowCount(0);
            for (String[] r : dao.list())
                model.addRow(r);
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }

    /** Collects account details and asks the authentication service to create a user. */
    private void create() {
        JTextField name = new JTextField(), username = new JTextField();
        JPasswordField pass = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(new String[] { "CASHIER", "ADMIN" });
        // Arrange labels and input controls in the account creation dialog.
        Object[] fields = { "Full name", name, "Username", username, "Password (8+ characters)", pass, "Role", role };
        if (JOptionPane.showConfirmDialog(this, fields, "Create user",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            new AuthService().createUser(admin, name.getText(), username.getText(), pass.getPassword(),
                    (String) role.getSelectedItem());
            load();
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }

    /** Enables or disables the selected account through the authentication service. */
    private void toggle() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Ui.info(this, "Select a user first.");
            return;
        }
        try {
            int modelRow = table.convertRowIndexToModel(row);
            long id = Long.parseLong(model.getValueAt(modelRow, 0).toString());
            boolean active = "Disabled".equals(model.getValueAt(modelRow, 4));
            new AuthService().setActive(admin, id, active);
            load();
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }
}
