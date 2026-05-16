package setup;

import VaultAuth.AuthController;
import javax.swing.*;


public class init {
    public static void startAuth() {
        SwingUtilities.invokeLater(() -> {
            AuthController controller = AuthController.getInstance();
            JFrame frame = controller.getAuthFrame();
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
