package auth.service;
import auth.dao.UserDao;
import java.sql.SQLException;
import shared.model.User;

public class AuthService {
    private final UserDao dao=new UserDao();
    public boolean needsAdminSetup()throws SQLException{return !dao.hasAdmin();}
    public User login(String username,char[] password)throws SQLException{return dao.login(username,password);}
    public void createInitialAdmin(String name,String username,char[] password)throws SQLException{if(!needsAdminSetup())throw new SQLException("An administrator is already configured.");validate(name,username,password);dao.create(name.trim(),username.trim(),password,"ADMIN");}
    public void createUser(User actor,String name,String username,char[] password,String role)throws SQLException{requireAdmin(actor);validate(name,username,password);if(!role.equals("ADMIN")&&!role.equals("CASHIER"))throw new SQLException("Invalid role.");dao.create(name.trim(),username.trim(),password,role);}
    public void setActive(User actor,long id,boolean active)throws SQLException{requireAdmin(actor);dao.setActive(id,active);}
    private void validate(String name,String username,char[] password)throws SQLException{if(name==null||name.isBlank()||username==null||!username.matches("[A-Za-z0-9_.-]{3,60}")||password==null||password.length<8)throw new SQLException("Enter a name, a username (3–60 letters/numbers/._-), and a password of at least 8 characters.");}
    public static void requireAdmin(User user)throws SQLException{if(user==null||!user.isAdmin())throw new SQLException("Administrator access is required.");}
}
