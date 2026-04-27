import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

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
    }
}