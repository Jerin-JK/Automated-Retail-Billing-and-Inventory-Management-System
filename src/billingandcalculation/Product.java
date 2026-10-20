package billingandcalculation;

public class Product {
    private final int productId;
    private final String name;
    private final double price;
    private int stock;
    private final double gstRate;

    public Product(int productId, String name, double price,
                   int stock, double gstRate) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.gstRate = gstRate;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public double getGstRate() {
        return gstRate;
    }

    public boolean reduceStock(int quantity) {
        if (quantity <= 0 || quantity > stock) {
            return false;
        }

        stock -= quantity;
        return true;
    }
}
