
package registration;

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
}
