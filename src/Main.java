import javax.swing.SwingUtilities;
import auth.ui.LoginFrame;

/** Application entry point. */
public final class Main {
    // Prevent instantiation; the application starts through main().
    private Main() {
    }

    /** Starts the sign-in window on Swing's event-dispatch thread. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
