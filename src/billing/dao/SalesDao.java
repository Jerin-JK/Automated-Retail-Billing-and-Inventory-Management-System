package billing.dao;

import shared.config.Database;
import java.math.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import billing.model.CartItem;
import inventory.model.Product;
import shared.model.User;

/** Persists sales, payment records, inventory deductions, and receipt data. */
public class SalesDao {
    /** Records a complete sale in one database transaction and returns its invoice number. */
    public String checkout(User cashier, List<CartItem> cart, String method, BigDecimal discount, BigDecimal paid)
            throws SQLException {
        if (cart.isEmpty())
            throw new SQLException("The cart is empty.");
        String invoice = "INV-" + java.time.LocalDate.now() + "-"
                + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT);
        // Use one connection so all sale and stock changes commit or roll back together.
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Calculate the pre-discount item subtotal and tax for the whole cart.
                BigDecimal subtotal = BigDecimal.ZERO, tax = BigDecimal.ZERO;
                for (CartItem item : cart) {
                    BigDecimal base = item.lineTotal();
                    subtotal = subtotal.add(base);
                    tax = tax.add(base.multiply(item.product.taxPercent).divide(new BigDecimal("100"), 2,
                            RoundingMode.HALF_UP));
                }
                subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
                tax = tax.setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = subtotal.subtract(discount).add(tax);
                if (discount.signum() < 0 || discount.compareTo(subtotal) > 0)
                    throw new SQLException("Discount must be between zero and subtotal.");
                if (paid.compareTo(total) < 0)
                    throw new SQLException("Payment received is less than the total.");
                // Insert the invoice header first so its generated ID can link all detail rows.
                long saleId;
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO sales(invoice_no,cashier_id,subtotal,discount_amount,tax_amount,total) VALUES(?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1, invoice);
                    p.setLong(2, cashier.getId());
                    p.setBigDecimal(3, subtotal);
                    p.setBigDecimal(4, discount);
                    p.setBigDecimal(5, tax);
                    p.setBigDecimal(6, total);
                    p.executeUpdate();
                    try (ResultSet r = p.getGeneratedKeys()) {
                        r.next();
                        saleId = r.getLong(1);
                    }
                }
                for (CartItem item : cart) {
                    try (PreparedStatement p = c.prepareStatement(
                            "UPDATE products SET stock_quantity=stock_quantity-? WHERE id=? AND active=TRUE AND stock_quantity>=?")) {
                        p.setBigDecimal(1, item.quantity);
                        p.setLong(2, item.product.id);
                        p.setBigDecimal(3, item.quantity);
                        if (p.executeUpdate() != 1)
                            throw new SQLException(
                                    "Insufficient stock for " + item.product.name + ". Refresh inventory and retry.");
                    }
                    BigDecimal base = item.lineTotal();
                    BigDecimal lineTax = base.multiply(item.product.taxPercent).divide(new BigDecimal("100"), 2,
                            RoundingMode.HALF_UP);
                    try (PreparedStatement p = c.prepareStatement(
                            "INSERT INTO sale_items(sale_id,product_id,product_name,sku,quantity,unit_price,tax_percent,line_total) VALUES(?,?,?,?,?,?,?,?)")) {
                        p.setLong(1, saleId);
                        p.setLong(2, item.product.id);
                        p.setString(3, item.product.name);
                        p.setString(4, item.product.sku);
                        p.setBigDecimal(5, item.quantity);
                        p.setBigDecimal(6, item.product.sellingPrice);
                        p.setBigDecimal(7, item.product.taxPercent);
                        p.setBigDecimal(8, base.add(lineTax));
                        p.executeUpdate();
                    }
                    try (PreparedStatement p = c.prepareStatement(
                            "INSERT INTO stock_movements(product_id,user_id,quantity_change,reason) VALUES(?,?,?,?)")) {
                        p.setLong(1, item.product.id);
                        p.setLong(2, cashier.getId());
                        p.setBigDecimal(3, item.quantity.negate());
                        p.setString(4, "Sale " + invoice);
                        p.executeUpdate();
                    }
                }
                try (PreparedStatement p = c
                        .prepareStatement("INSERT INTO payments(sale_id,method,amount) VALUES(?,?,?)")) {
                    p.setLong(1, saleId);
                    p.setString(2, method);
                    p.setBigDecimal(3, paid);
                    p.executeUpdate();
                }
                // Make the sale, payment, item, and stock changes permanent together.
                c.commit();
                return invoice;
            // Undo every change if any part of checkout fails.
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Loads invoice and payment rows for the selected calendar date. */
    public List<String[]> salesForDate(LocalDate date) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT s.invoice_no,s.created_at,u.full_name,s.subtotal,s.discount_amount,s.tax_amount,s.total,p.method,p.amount FROM sales s JOIN users u ON u.id=s.cashier_id LEFT JOIN payments p ON p.sale_id=s.id WHERE DATE(s.created_at)=? ORDER BY s.created_at DESC";
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet r = p.executeQuery()) {
                while (r.next())
                    rows.add(new String[] { r.getString(1), r.getString(2), r.getString(3), r.getString(4),
                            r.getString(5), r.getString(6), r.getString(7), r.getString(8), r.getString(9) });
            }
        }
        return rows;
    }

    /** Sums sale totals for the selected calendar date. */
    public BigDecimal totalForDate(LocalDate date) throws SQLException {
        try (Connection c = Database.getConnection();
                PreparedStatement p = c
                        .prepareStatement("SELECT COALESCE(SUM(total),0) FROM sales WHERE DATE(created_at)=?")) {
            p.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getBigDecimal(1);
            }
        }
    }

    /** Builds printable receipt text from the invoice header and its item rows. */
    public String receipt(String invoice) throws SQLException {
        StringBuilder out = new StringBuilder();
        // Fetch the invoice summary and cashier before loading its line items.
        String head = "SELECT s.invoice_no,s.created_at,u.full_name,s.subtotal,s.discount_amount,s.tax_amount,s.total,p.method,p.amount,s.id FROM sales s JOIN users u ON u.id=s.cashier_id LEFT JOIN payments p ON p.sale_id=s.id WHERE s.invoice_no=?";
        long id;
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(head)) {
            p.setString(1, invoice);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next())
                    throw new SQLException("Invoice not found.");
                id = r.getLong(10);
                out.append("SUPERMARKET\nInvoice: ").append(r.getString(1)).append("\nDate: ").append(r.getString(2))
                        .append("\nCashier: ").append(r.getString(3)).append("\n\n");
                out.append("Subtotal: ").append(r.getString(4)).append("\nTax: ").append(r.getString(6))
                        .append("\nDiscount: ").append(r.getString(5)).append("\nTOTAL: ").append(r.getString(7))
                        .append("\nPayment: ").append(r.getString(8)).append(" / ").append(r.getString(9))
                        .append("\n\n");
            }
        }
        try (Connection c = Database.getConnection();
                PreparedStatement p = c.prepareStatement(
                        "SELECT product_name,quantity,unit_price,line_total FROM sale_items WHERE sale_id=? ORDER BY id")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next())
                    out.append(r.getString(1)).append(" x ").append(r.getString(2)).append(" @ ").append(r.getString(3))
                            .append(" = ").append(r.getString(4)).append('\n');
            }
        }
        return out.toString();
    }
}
