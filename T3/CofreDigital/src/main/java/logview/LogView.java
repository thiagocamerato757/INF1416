package logview;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.swing.*;
import java.security.Security;

public class LogView {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        String defaultPath = args.length > 0 ? args[0] : null;

        SwingUtilities.invokeLater(() -> {
            LogViewFrame frame = new LogViewFrame(defaultPath);
            frame.setVisible(true);
        });
    }
}
