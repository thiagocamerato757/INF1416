package VaultAuth.UI;

import UI.UIUtils;
import VaultAuth.AuthController;
import VaultAuth.TOTP;
import logger.Logger;

import javax.swing.*;
import java.awt.*;

public class TOTPPanel extends JPanel {
    private JTextField totpField;
    private JLabel info;

    public TOTPPanel() {
        super(new BorderLayout());
        AuthController.getInstance().getUser().ifPresent(u -> Logger.log(4001, u.getUid(), u.getLogin()));
        setup();
    }

    private void setup() {
        setBackground(UIUtils.COLOR_BACKGROUND);
        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(500);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Token TOTP"));
        header.add(UIUtils.createLabel("Informe o codigo de 6 dígitos do Google Authenticator."));
        card.add(header, BorderLayout.NORTH);

        totpField = UIUtils.createTextField(12);
        JPanel form = UIUtils.createFormPanel();
        UIUtils.addFormRow(form, 0, "Token:", totpField);
        card.add(form, BorderLayout.CENTER);

        JButton proceedButton = UIUtils.createButton("Prosseguir", UIUtils.COLOR_SUCCESS);
        proceedButton.addActionListener(e -> {
            TOTP totp = TOTP.getInstance();
            totp.validateCode(totpField.getText().trim());
            info.setText(totp.getFeedbackMessage());
            totpField.setText("");
            AuthController.getInstance().Check();
        });
        info = UIUtils.createLabel("");
        info.setForeground(UIUtils.COLOR_DANGER);

        JPanel footer = UIUtils.createWhitePanel(new BorderLayout(), 0);
        footer.add(UIUtils.createButtonRow(proceedButton), BorderLayout.NORTH);
        footer.add(info, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        UIUtils.addCenteredCard(container, card);
        add(container, BorderLayout.CENTER);
    }
}
