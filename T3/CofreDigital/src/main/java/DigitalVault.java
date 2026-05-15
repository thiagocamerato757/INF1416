import db.DataBaseStarter;
import logger.Logger;
import db.dao.UserDAO;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import javax.swing.*;

import VaultAuth.AdminController;
import VaultAuth.AuthController;

public class DigitalVault {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AdminController.clearAdminSecretPhrase();
        }));

        DataBaseStarter.testConnection();

        SwingUtilities.invokeLater(() -> {
            Logger.log(1001, "Sistema iniciado.");

            if (UserDAO.checkAnyUser()) {
                VaultAuth.UI.AdminLoginFrame loginFrame = new VaultAuth.UI.AdminLoginFrame();
                loginFrame.setVisible(true);
            } else {
                VaultAuth.UI.AdminSetupFrame setupFrame = new VaultAuth.UI.AdminSetupFrame();
                setupFrame.setVisible(true);
            }
        });
    }
}
