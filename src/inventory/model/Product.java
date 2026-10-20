package inventory.model;
import java.math.BigDecimal;
public class Product {
    public long id; public String sku, barcode, name, category, unit;
    public BigDecimal purchasePrice, sellingPrice, taxPercent, stock, reorderLevel;
    public Product(long id,String sku,String barcode,String name,String category,String unit,BigDecimal purchasePrice,BigDecimal sellingPrice,BigDecimal taxPercent,BigDecimal stock,BigDecimal reorderLevel){
        this.id=id;this.sku=sku;this.barcode=barcode;this.name=name;this.category=category;this.unit=unit;this.purchasePrice=purchasePrice;this.sellingPrice=sellingPrice;this.taxPercent=taxPercent;this.stock=stock;this.reorderLevel=reorderLevel;
    }
}
