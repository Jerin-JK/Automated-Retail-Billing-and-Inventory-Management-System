package app.ui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import auth.ui.LoginFrame;
import billing.ui.BillingPanel;
import inventory.ui.InventoryPanel;
import reports.ui.ReportsPanel;
import shared.model.User;
import shared.ui.Ui;
import suppliers.ui.SuppliersPanel;
import users.ui.UsersPanel;

/** Shows the application sections available to the signed-in user. */
public class DashboardFrame extends JFrame {
    /** Builds the tabbed dashboard and its sign-out controls. */
    public DashboardFrame(User user) {
        setTitle("Supermarket Billing — " + user.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Create the tabs shared by all signed-in users.
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Billing", new BillingPanel(user));
        tabs.addTab("Inventory", new InventoryPanel(user));
        // Expose administration and reporting screens only to administrators.
        if (user.isAdmin()) {
            tabs.addTab("Sales & Reports", new ReportsPanel(user));
            tabs.addTab("Users", new UsersPanel(user));
            tabs.addTab("Suppliers", new SuppliersPanel(user));
        }
        // Place the current user identity and sign-out action above the tabs.
        JPanel head = new JPanel(new BorderLayout());
        head.add(new JLabel("  " + user.getName() + "  •  " + user.getRole()), BorderLayout.WEST);
        // Return to the sign-in screen when the user signs out.
        JButton logout = new JButton("Sign out");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        head.add(logout, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);
        add(tabs);
        // Apply the shared window size and centering defaults.
        Ui.style(this);
        setSize(1150, 720);
    }
}
