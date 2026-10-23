package shared.model;

/** Represents a signed-in account and its authorization role. */
public class User {
    // Immutable account identity and display/authentication attributes.
    private final long id;
    private final String name, username, role;

    /** Stores the account values returned by authentication. */
    public User(long id, String name, String username, String role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.role = role;
    }

    /** Returns the database ID for this account. */
    public long getId() {
        return id;
    }

    /** Returns the account holder's display name. */
    public String getName() {
        return name;
    }

    /** Returns the sign-in username. */
    public String getUsername() {
        return username;
    }

    /** Returns the account role name. */
    public String getRole() {
        return role;
    }

    /** Reports whether this account has the ADMIN role. */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
