package VaultAuth.UI;

import VaultAuth.AuthController;
import VaultAuth.TOTP;

import javax.swing.*;

public class TOTPPanel extends JPanel {
    private JTextField totpField;

    public TOTPPanel() {
        super();
        setup();
    }

    private void setup() {
        JLabel totpLabel = new JLabel("TOTP");
        add(totpLabel);

        totpField = new JTextField(20);
        add(totpField);

        JButton proceedButton = new JButton("Proceed");
        proceedButton.addActionListener(e -> {
            TOTP totp = TOTP.getInstance();
            totp.validateCode(totpField.getText());

            totpField.setText("");

            AuthController ctrl = AuthController.getInstance();
            ctrl.Check();
        });
        add(proceedButton);
    }
}
