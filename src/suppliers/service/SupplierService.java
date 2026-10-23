package suppliers.service;

import suppliers.dao.SupplierDao;
import java.sql.SQLException;
import java.util.List;
import shared.model.User;
import auth.service.AuthService;

/** Enforces supplier input and administrator access rules. */
public class SupplierService {
    // Database operations used after service-level checks.
    private final SupplierDao dao = new SupplierDao();

    /** Requires administrator access before listing suppliers. */
    public List<String[]> list(User actor) throws SQLException {
        AuthService.requireAdmin(actor);
        return dao.list();
    }

    /** Requires administrator access and a non-empty name before insertion. */
    public void add(User actor, String name, String phone, String email, String address) throws SQLException {
        AuthService.requireAdmin(actor);
        if (name == null || name.isBlank())
            throw new SQLException("Supplier name is required.");
        dao.add(name.trim(), phone, email, address);
    }

    /** Requires administrator access before changing supplier status. */
    public void setActive(User actor, long id, boolean active) throws SQLException {
        AuthService.requireAdmin(actor);
        dao.setActive(id, active);
    }
}
