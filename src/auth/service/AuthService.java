package auth.service;

import auth.dao.UserDao;
import java.sql.SQLException;
import shared.model.User;

/** Applies account rules and authorization before delegating to the user data layer. */
public class AuthService {
    // Database access used by the authentication operations below.
    private final UserDao dao = new UserDao();

    /** Reports whether the database has an administrator account. */
    public boolean needsAdminSetup() throws SQLException {
        return !dao.hasAdmin();
    }

    /** Authenticates credentials through the user data layer. */
    public User login(String username, char[] password) throws SQLException {
        return dao.login(username, password);
    }

    /** Creates the first administrator only while setup is incomplete. */
    public void createInitialAdmin(String name, String username, char[] password) throws SQLException {
        if (!needsAdminSetup())
            throw new SQLException("An administrator is already configured.");
        validate(name, username, password);
        dao.create(name.trim(), username.trim(), password, "ADMIN");
    }

    /** Validates an administrator request before creating another account. */
    public void createUser(User actor, String name, String username, char[] password, String role) throws SQLException {
        requireAdmin(actor);
        validate(name, username, password);
        // Reject role names outside the two account types supported by the application.
        if (!role.equals("ADMIN") && !role.equals("CASHIER"))
            throw new SQLException("Invalid role.");
        dao.create(name.trim(), username.trim(), password, role);
    }

    /** Allows an administrator to change a user account's active state. */
    public void setActive(User actor, long id, boolean active) throws SQLException {
        requireAdmin(actor);
        dao.setActive(id, active);
    }

    /** Enforces required names, username format, and minimum password length. */
    private void validate(String name, String username, char[] password) throws SQLException {
        if (name == null || name.isBlank() || username == null || !username.matches("[A-Za-z0-9_.-]{3,60}")
                || password == null || password.length < 8)
            throw new SQLException(
                    "Enter a name, a username (3–60 letters/numbers/._-), and a password of at least 8 characters.");
    }

    /** Stops the operation unless the supplied user has administrator access. */
    public static void requireAdmin(User user) throws SQLException {
        if (user == null || !user.isAdmin())
            throw new SQLException("Administrator access is required.");
    }
}
