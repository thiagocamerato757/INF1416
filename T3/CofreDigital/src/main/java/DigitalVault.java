import db.DataBaseStarter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import javax.swing.*;

import VaultAuth.AuthController;
import VaultAuth.UI.AuthFrame;

/**
 * Main entry point for the Cofre-Digital application.
 * Currently only tests the database connection.
 */
public class DigitalVault {

    public static void main(String[] args) {
        // Register Bouncy Castle security provider (required for bcrypt)
        Security.addProvider(new BouncyCastleProvider());

        // Test database connection
        DataBaseStarter.testConnection();

        // UI init
        SwingUtilities.invokeLater(() -> {
            AuthController controller = AuthController.getInstance();
            JFrame frame = controller.getAuthFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setVisible(true);
        });
    }
}