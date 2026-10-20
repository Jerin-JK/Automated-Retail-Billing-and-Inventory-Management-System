package auth.ui;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import shared.model.User;
import auth.service.AuthService;
import shared.ui.Ui;
import app.ui.DashboardFrame;

public class LoginFrame extends JFrame {
    private final AuthService auth=new AuthService();
    private final JTextField username=new JTextField(22);
    private final JPasswordField password=new JPasswordField(22);
    private final JLabel status=new JLabel(" ");
    public LoginFrame(){setTitle("Supermarket Billing — Sign in");setDefaultCloseOperation(EXIT_ON_CLOSE);JPanel panel=new JPanel(new GridBagLayout());panel.setBorder(new EmptyBorder(30,40,30,40));GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(9,8,9,8);g.fill=GridBagConstraints.HORIZONTAL;
        JLabel title=new JLabel("Supermarket Billing",SwingConstants.CENTER);title.setFont(title.getFont().deriveFont(Font.BOLD,24f));g.gridwidth=2;g.gridx=0;g.gridy=0;panel.add(title,g);g.gridwidth=1;g.gridy++;
        g.gridx=0;panel.add(new JLabel("Username"),g);g.gridx=1;panel.add(username,g);g.gridy++;g.gridx=0;panel.add(new JLabel("Password"),g);g.gridx=1;panel.add(password,g);g.gridx=0;g.gridy++;g.gridwidth=2;status.setForeground(new Color(170,40,40));panel.add(status,g);g.gridy++;
        JButton login=new JButton("Sign in");g.gridx=0;g.gridwidth=2;panel.add(login,g);login.addActionListener(e->doLogin(login));password.addActionListener(e->doLogin(login));add(panel);pack();setMinimumSize(new Dimension(430,290));setLocationRelativeTo(null);checkSetup();}
    private void checkSetup(){try{if(auth.needsAdminSetup())SwingUtilities.invokeLater(this::setupAdmin);}catch(SQLException ex){status.setText("Database unavailable. Configure MySQL and restart.");}}
    private void setupAdmin(){JTextField name=new JTextField();JTextField user=new JTextField();JPasswordField pass=new JPasswordField();JPasswordField confirm=new JPasswordField();Object[] fields={"Create the first administrator", "Full name",name,"Username",user,"Password (8+ characters)",pass,"Confirm password",confirm};while(true){int result=JOptionPane.showConfirmDialog(this,fields,"Initial administrator setup",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);if(result!=JOptionPane.OK_OPTION)return;if(!java.util.Arrays.equals(pass.getPassword(),confirm.getPassword())){Ui.error(this,new IllegalArgumentException("Passwords do not match."));continue;}try{auth.createInitialAdmin(name.getText(),user.getText(),pass.getPassword());Ui.info(this,"Administrator created. Sign in to continue.");return;}catch(Exception ex){Ui.error(this,ex);}}}
    private void doLogin(JButton button){button.setEnabled(false);char[] secret=password.getPassword();String u=username.getText().trim();status.setText("Signing in…");new SwingWorker<User,Void>(){protected User doInBackground()throws Exception{return auth.login(u,secret);}protected void done(){button.setEnabled(true);java.util.Arrays.fill(secret,'\0');try{User user=get();if(user==null){status.setText("Incorrect username or password, or account disabled.");return;}dispose();new DashboardFrame(user).setVisible(true);}catch(Exception ex){Throwable c=ex.getCause()==null?ex:ex.getCause();Ui.error(LoginFrame.this,c);status.setText("Could not sign in.");}}}.execute();}
}
