import javax.swing.SwingUtilities;
import auth.ui.LoginFrame;

/** Application entry point. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
