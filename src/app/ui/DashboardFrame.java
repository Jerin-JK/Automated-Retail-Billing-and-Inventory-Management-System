package app.ui;
import java.awt.BorderLayout;
import javax.swing.*;
import shared.model.User;
import auth.ui.LoginFrame;
import shared.ui.Ui;
import billing.ui.BillingPanel;
import inventory.ui.InventoryPanel;
import reports.ui.ReportsPanel;
import users.ui.UsersPanel;
import suppliers.ui.SuppliersPanel;

public class DashboardFrame extends JFrame {
    public DashboardFrame(User user){setTitle("Supermarket Billing — "+user.getName());setDefaultCloseOperation(EXIT_ON_CLOSE);JTabbedPane tabs=new JTabbedPane();tabs.addTab("Billing",new BillingPanel(user));tabs.addTab("Inventory",new InventoryPanel(user));if(user.isAdmin()){tabs.addTab("Sales & Reports",new ReportsPanel(user));tabs.addTab("Users",new UsersPanel(user));tabs.addTab("Suppliers",new SuppliersPanel(user));}
        JPanel head=new JPanel(new BorderLayout());head.add(new JLabel("  "+user.getName()+"  •  "+user.getRole()),BorderLayout.WEST);JButton logout=new JButton("Sign out");logout.addActionListener(e->{dispose();new LoginFrame().setVisible(true);});head.add(logout,BorderLayout.EAST);add(head,BorderLayout.NORTH);add(tabs);Ui.style(this);setSize(1150,720);}
}
