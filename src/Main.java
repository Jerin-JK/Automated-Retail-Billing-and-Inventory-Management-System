
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import registration.RegisterFrame;

public class Main extends JFrame
        implements FocusListener, ActionListener {

    JLabel status;
    JButton login, register;
    JTextField ut;
    JPasswordField pt;

    Main() {

        Container c = getContentPane();
        c.setLayout(new FlowLayout());

        JLabel ul = new JLabel("Username");
        ut = new JTextField(20);

        JLabel pl = new JLabel("Password");
        pt = new JPasswordField(20);

        login = new JButton("Login");
        register = new JButton("Register");

        status = new JLabel("");

        c.add(ul);
        c.add(ut);
        c.add(pl);
        c.add(pt);
        c.add(status);
        c.add(login);
        c.add(register);

        ut.addFocusListener(this);
        login.addActionListener(this);
        register.addActionListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        status.setText("Focus gained ...!!!");
    }

    @Override
    public void focusLost(FocusEvent e) {
        status.setText("Focus lost ...!!!");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == login) {

            String username = ut.getText().trim();
            String password = new String(pt.getPassword());

            String sql =
                "SELECT * FROM users WHERE username = ? AND password_hash = ?";

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                try (Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/retail_db",
                        "root",
                        "5821");

                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, username);
                    ps.setString(2, password);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            status.setText("Login Successful");
                        } else {
                            status.setText(
                                "Invalid Username or Password");
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                status.setText("Database Error");
            }
        }

        if (e.getSource() == register) {
            RegisterFrame rf = new RegisterFrame();
            rf.setVisible(true);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Main obj = new Main();

            obj.setTitle("Login Window");
            obj.setSize(500, 300);
            obj.setLocationRelativeTo(null);
            obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            obj.setVisible(true);
        });
    }
}
//test error
