package shared.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Provides shared window styling and message dialog helpers. */
public final class Ui {
    // Prevent instantiation of this static UI helper.
    private Ui() {
    }

    /** Creates a panel with the standard layout and padding. */
    public static JPanel page() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        return p;
    }

    /** Shows a concise error message owned by the supplied window or panel. */
    public static void error(Component parent, Throwable ex) {
        String m = ex.getMessage();
        if (m == null || m.isBlank())
            m = ex.getClass().getSimpleName();
        JOptionPane.showMessageDialog(parent, m, "Operation failed", JOptionPane.ERROR_MESSAGE);
    }

    /** Shows an informational message owned by the supplied component. */
    public static void info(Component parent, String m) {
        JOptionPane.showMessageDialog(parent, m, "Supermarket", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Applies the shared minimum window size and centers the frame. */
    public static void style(JFrame f) {
        f.setMinimumSize(new Dimension(880, 560));
        f.setLocationRelativeTo(null);
    }
}
