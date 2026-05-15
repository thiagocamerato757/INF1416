package setup;

import VaultAuth.AuthController;
import crypto.PasswordUtil;
import VaultAuth.TOTPKeyManager;
import Cryptography.Base32;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.nio.file.Paths;
import java.security.Security;

public class init {
    public static void startAuth() {
        SwingUtilities.invokeLater(() -> {
            AuthController controller = AuthController.getInstance();
            JFrame frame = controller.getAuthFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setVisible(true);
        });
    }
}
