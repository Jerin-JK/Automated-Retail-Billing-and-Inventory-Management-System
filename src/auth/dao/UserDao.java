package auth.dao;

import shared.config.Database;
import java.sql.*;
import java.util.*;
import shared.model.User;
import auth.security.PasswordHasher;

/** Reads and writes user accounts in the database. */
public class UserDao {
    /** Checks whether first-run administrator setup is still needed. */
    public boolean hasAdmin() throws SQLException {
        try (Connection c = Database.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT 1 FROM users WHERE role='ADMIN' LIMIT 1");
                ResultSet r = p.executeQuery()) {
            return r.next();
        }
    }

    /** Returns an active account only when its stored password hash matches. */
    public User login(String username, char[] password) throws SQLException {
        String sql = "SELECT id,full_name,username,password_hash,role FROM users WHERE username=? AND active=TRUE";
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next() || !PasswordHasher.verify(password, r.getString("password_hash")))
                    return null;
                return new User(r.getLong("id"), r.getString("full_name"), r.getString("username"),
                        r.getString("role"));
            }
        }
    }

    /** Creates an account after hashing its password. */
    public void create(String name, String username, char[] password, String role) throws SQLException {
        String sql = "INSERT INTO users(full_name,username,password_hash,role) VALUES(?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, username);
            p.setString(3, PasswordHasher.hash(password));
            p.setString(4, role);
            p.executeUpdate();
        }
    }

    /** Loads account details and display-ready active status for the users table. */
    public List<String[]> list() throws SQLException {
        List<String[]> users = new ArrayList<>();
        try (Connection c = Database.getConnection();
                PreparedStatement p = c
                        .prepareStatement("SELECT id,full_name,username,role,active FROM users ORDER BY full_name");
                ResultSet r = p.executeQuery()) {
            while (r.next())
                users.add(new String[] { r.getString(1), r.getString(2), r.getString(3), r.getString(4),
                        r.getBoolean(5) ? "Active" : "Disabled" });
        }
        return users;
    }

    /** Enables or disables a non-administrator account. */
    public void setActive(long id, boolean active) throws SQLException {
        try (Connection c = Database.getConnection();
                PreparedStatement p = c.prepareStatement("UPDATE users SET active=? WHERE id=? AND role<>'ADMIN'")) {
            p.setBoolean(1, active);
            p.setLong(2, id);
            p.executeUpdate();
        }
    }
}
