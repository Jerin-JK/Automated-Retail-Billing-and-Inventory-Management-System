package billing.model;

import java.math.BigDecimal;
import inventory.model.Product;

/** Pairs a product with the quantity currently held in the checkout cart. */
public class CartItem {
    // Product details used to price and display this cart line.
    public final Product product;
    // Quantity can increase when the same product is added again.
    public BigDecimal quantity;

    /** Creates one cart line for a product and its starting quantity. */
    public CartItem(Product product, BigDecimal quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    /** Returns the quantity multiplied by the selling price, rounded to cents. */
    public BigDecimal lineTotal() {
        return product.sellingPrice.multiply(quantity).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
