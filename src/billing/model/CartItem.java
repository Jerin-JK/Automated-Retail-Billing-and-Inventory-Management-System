package billing.model;
import java.math.BigDecimal;
import inventory.model.Product;
public class CartItem {
    public final Product product; public BigDecimal quantity;
    public CartItem(Product product, BigDecimal quantity){this.product=product;this.quantity=quantity;}
    public BigDecimal lineTotal(){return product.sellingPrice.multiply(quantity).setScale(2,java.math.RoundingMode.HALF_UP);}
}
