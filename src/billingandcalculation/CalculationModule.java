package billingandcalculation;

import java.util.Collection;

public class CalculationModule {

    public double calculateSubtotal(Collection<CartItem> items) {
        double subtotal = 0.0;

        for (CartItem item : items) {
            subtotal += item.getSubtotal();
        }

        return subtotal;
    }

    public double calculateGST(Collection<CartItem> items) {
        double totalGST = 0.0;

        for (CartItem item : items) {
            totalGST += item.getGST();
        }

        return totalGST;
    }

    public double calculateDiscount(double subtotal,
                                    double discountRate) {
        if (discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException(
                "Discount must be between 0 and 100."
            );
        }

        return subtotal * discountRate / 100.0;
    }

    public double calculateFinalTotal(double subtotal,
                                       double gst,
                                       double discount) {
        return Math.max(0.0, subtotal + gst - discount);
    }
}
