package billingandcalculation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BillingModule {
    private final Map<Integer, Product> products = new LinkedHashMap<>();
    private final Map<Integer, CartItem> cart = new LinkedHashMap<>();
    private final CalculationModule calculator = new CalculationModule();

    public BillingModule() {
        // Sample products for testing.
        addProduct(new Product(101, "Rice", 60.00, 50, 5));
        addProduct(new Product(102, "Sugar", 45.00, 40, 5));
        addProduct(new Product(103, "Soap", 35.00, 30, 18));
        addProduct(new Product(104, "Milk", 30.00, 40, 5));
        addProduct(new Product(105, "Cooking Oil", 150.00, 25, 5));
        addProduct(new Product(106, "Biscuits", 20.00, 60, 5));
    }

    public void addProduct(Product product) {
        products.put(product.getProductId(), product);
    }

    public Collection<Product> getProducts() {
        return products.values();
    }

    public Product findProduct(int productId) {
        return products.get(productId);
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cart.values());
    }

    public void addToCart(int productId, int quantity) {
        Product product = products.get(productId);

        if (product == null) {
            throw new IllegalArgumentException("Product ID not found.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be greater than zero."
            );
        }

        CartItem existing = cart.get(productId);
        int oldQuantity = existing == null ? 0 : existing.getQuantity();

        if (oldQuantity + quantity > product.getStock()) {
            throw new IllegalArgumentException(
                "Insufficient stock. Available: "
                + (product.getStock() - oldQuantity)
            );
        }

        if (existing == null) {
            cart.put(productId, new CartItem(product, quantity));
        } else {
            existing.setQuantity(oldQuantity + quantity);
        }
    }

    public void updateQuantity(int productId, int quantity) {
        CartItem item = cart.get(productId);

        if (item == null) {
            throw new IllegalArgumentException(
                "Product is not in the cart."
            );
        }

        if (quantity < 0 || quantity > item.getProduct().getStock()) {
            throw new IllegalArgumentException(
                "Invalid quantity or insufficient stock."
            );
        }

        if (quantity == 0) {
            cart.remove(productId);
        } else {
            item.setQuantity(quantity);
        }
    }

    public void removeFromCart(int productId) {
        if (cart.remove(productId) == null) {
            throw new IllegalArgumentException(
                "Product is not in the cart."
            );
        }
    }

    public double getSubtotal() {
        return calculator.calculateSubtotal(cart.values());
    }

    public double getGST() {
        return calculator.calculateGST(cart.values());
    }

    public double getDiscount(double rate) {
        return calculator.calculateDiscount(getSubtotal(), rate);
    }

    public double getFinalTotal(double rate) {
        return calculator.calculateFinalTotal(
            getSubtotal(), getGST(), getDiscount(rate)
        );
    }

    public void completeSale() {
        if (cart.isEmpty()) {
            throw new IllegalStateException("The cart is empty.");
        }

        // Validate all items before changing any stock.
        for (CartItem item : cart.values()) {
            if (item.getQuantity() > item.getProduct().getStock()) {
                throw new IllegalStateException(
                    "Insufficient stock for " + item.getProduct().getName()
                );
            }
        }

        for (CartItem item : cart.values()) {
            item.getProduct().reduceStock(item.getQuantity());
        }

        cart.clear();
    }

    public void clearCart() {
        cart.clear();
    }
}
