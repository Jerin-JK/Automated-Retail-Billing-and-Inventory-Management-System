package registration;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterFrame extends JFrame {

    JTextField nameField = new JTextField();
    JTextField emailField = new JTextField();
    JTextField userField = new JTextField();
    JPasswordField passField = new JPasswordField();

    public RegisterFrame() {
        setTitle("Register");

        Container c = getContentPane();
        c.setLayout(new GridLayout(5, 2, 10, 10));

        c.add(new JLabel("Name"));
        c.add(nameField);
        c.add(new JLabel("Email"));
        c.add(emailField);
        c.add(new JLabel("Username"));
        c.add(userField);
        c.add(new JLabel("Password"));
        c.add(passField);

        JButton save = new JButton("Register");
        c.add(save);

        save.addActionListener(e -> registerUser());

        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    void registerUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields");
            return;
        }

        String sql = "INSERT INTO users (name, email, username, password_hash, role) "
                   + "VALUES (?, ?, ?, ?, 'cashier')";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/retail_billing",
                    "root",
                    "54321");
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, username);
                ps.setString(4, password);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Registered successfully");
                dispose();
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Username already taken");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
/*package registration;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    public RegisterFrame() {

        setTitle("Register");

        Container c = getContentPane();
        c.setLayout(new GridLayout(5, 2, 10, 10));

        c.add(new JLabel("Name"));
        c.add(new JTextField());

        c.add(new JLabel("Email"));
        c.add(new JTextField());

        c.add(new JLabel("Username"));
        c.add(new JTextField());

        c.add(new JLabel("Password"));
        c.add(new JPasswordField());

        JButton save = new JButton("Register");
        c.add(save);

        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}*/
