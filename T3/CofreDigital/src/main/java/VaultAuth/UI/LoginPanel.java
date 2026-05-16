package VaultAuth.UI;

import UI.UIUtils;
import VaultAuth.AuthController;
import VaultAuth.LoginAuth;
import logger.Logger;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField email;
    private JLabel info;

    public LoginPanel() {
        super(new BorderLayout());
        Logger.log(2001);
        setup();
    }

    private void setup() {
        setBackground(UIUtils.COLOR_BACKGROUND);
        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(500);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Autenticação"));
        header.add(UIUtils.createLabel("Informe seu e-mail para iniciar o acesso ao Cofre Digital."));
        card.add(header, BorderLayout.NORTH);

        email = UIUtils.createTextField(28);
        JPanel form = UIUtils.createFormPanel();
        UIUtils.addFormRow(form, 0, "E-mail:", email);
        card.add(form, BorderLayout.CENTER);

        JButton proceed = UIUtils.createButton("Prosseguir", UIUtils.COLOR_PRIMARY);
        proceed.addActionListener(e -> {
            LoginAuth auth = LoginAuth.getInstance();
            auth.validateLogin(email.getText().trim());
            email.setText("");
            info.setText(auth.getFeedbackMessage());
            AuthController.getInstance().Check();
        });
        info = UIUtils.createLabel("");
        info.setForeground(UIUtils.COLOR_DANGER);

        JPanel footer = UIUtils.createWhitePanel(new BorderLayout(), 0);
        footer.add(UIUtils.createButtonRow(proceed), BorderLayout.NORTH);
        footer.add(info, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        UIUtils.addCenteredCard(container, card);
        add(container, BorderLayout.CENTER);
    }
}
