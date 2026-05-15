package VaultAuth.UI;

import VaultAuth.AuthController;
import VaultAuth.TOTP;

import javax.swing.*;
import java.awt.*;

public class TOTPPanel extends JPanel {
    private JTextField totpField;
    private JLabel info;

    public TOTPPanel() {
        super();
        setup();
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel totpLabel = new JLabel("TOTP:");
        totpField = new JTextField(20);
        topRow.add(totpLabel);
        topRow.add(totpField);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton proceedButton = new JButton("Proceed");
        proceedButton.addActionListener(e -> {
            TOTP totp = TOTP.getInstance();
            totp.validateCode(totpField.getText());

            info.setText(totp.getFeedbackMessage());
            totpField.setText("");

            AuthController ctrl = AuthController.getInstance();
            ctrl.Check();
        });
        buttonRow.add(proceedButton);

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        info = new JLabel("");
        infoRow.add(info);

        add(topRow);
        add(buttonRow);
        add(infoRow);
    }
}
