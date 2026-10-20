package billing.service;
import billing.dao.SalesDao;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import billing.model.CartItem;
import shared.model.User;
import auth.service.AuthService;

public class SalesService {
    private final SalesDao dao=new SalesDao();
    public String checkout(User cashier,List<CartItem> cart,String method,BigDecimal discount,BigDecimal paid)throws SQLException{if(cashier==null)throw new SQLException("Please sign in again.");return dao.checkout(cashier,cart,method,discount,paid);}
    public List<String[]> salesForDate(User actor,LocalDate date)throws SQLException{AuthService.requireAdmin(actor);return dao.salesForDate(date);}
    public BigDecimal totalForDate(User actor,LocalDate date)throws SQLException{AuthService.requireAdmin(actor);return dao.totalForDate(date);}
    public String receipt(User actor,String invoice)throws SQLException{AuthService.requireAdmin(actor);return dao.receipt(invoice);}
}
