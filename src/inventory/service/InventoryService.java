package inventory.service;
import inventory.dao.ProductDao;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import inventory.model.Product;
import shared.model.User;
import auth.service.AuthService;

public class InventoryService {
    private final ProductDao dao=new ProductDao();
    public List<Product> search(String text)throws SQLException{return dao.search(text==null?"":text.trim());}
    public Product find(long id)throws SQLException{return dao.find(id);}
    public void save(User actor,Product product)throws SQLException{AuthService.requireAdmin(actor);if(product.sku==null||product.sku.isBlank()||product.name==null||product.name.isBlank()||product.unit==null||product.unit.isBlank()||product.sellingPrice==null||product.sellingPrice.signum()<0||product.purchasePrice==null||product.purchasePrice.signum()<0||product.taxPercent==null||product.taxPercent.signum()<0||product.stock==null||product.stock.signum()<0||product.reorderLevel==null||product.reorderLevel.signum()<0)throw new SQLException("Product fields are invalid. Prices, tax, stock and reorder level cannot be negative.");dao.save(product,actor.getId());}
    public void delete(User actor,long id)throws SQLException{AuthService.requireAdmin(actor);dao.delete(id);}
    public void deactivate(User actor,long id)throws SQLException{AuthService.requireAdmin(actor);dao.deactivate(id);}
    public void adjust(User actor,long id,BigDecimal change,String reason)throws SQLException{AuthService.requireAdmin(actor);if(change==null||change.signum()==0||reason==null||reason.isBlank())throw new SQLException("Enter a non-zero adjustment and its reason.");dao.adjust(id,change,actor.getId(),reason.trim());}
}
