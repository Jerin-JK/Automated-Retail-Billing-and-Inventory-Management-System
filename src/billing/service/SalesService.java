package billing.service;

import billing.dao.SalesDao;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import billing.model.CartItem;
import shared.model.User;
import auth.service.AuthService;

/** Checks sale access rules and delegates billing operations to SalesDao. */
public class SalesService {
    // Database operations used by checkout and report requests.
    private final SalesDao dao = new SalesDao();

    /** Requires a signed-in cashier before recording the sale. */
    public String checkout(User cashier, List<CartItem> cart, String method, BigDecimal discount, BigDecimal paid)
            throws SQLException {
        if (cashier == null)
            throw new SQLException("Please sign in again.");
        return dao.checkout(cashier, cart, method, discount, paid);
    }

    /** Restricts daily sale listings to administrators. */
    public List<String[]> salesForDate(User actor, LocalDate date) throws SQLException {
        AuthService.requireAdmin(actor);
        return dao.salesForDate(date);
    }

    /** Restricts daily totals to administrators. */
    public BigDecimal totalForDate(User actor, LocalDate date) throws SQLException {
        AuthService.requireAdmin(actor);
        return dao.totalForDate(date);
    }

    /** Restricts receipt retrieval to administrators. */
    public String receipt(User actor, String invoice) throws SQLException {
        AuthService.requireAdmin(actor);
        return dao.receipt(invoice);
    }
}
