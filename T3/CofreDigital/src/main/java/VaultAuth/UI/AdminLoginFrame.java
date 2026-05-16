package VaultAuth.UI;

import UI.UIUtils;
import VaultAuth.AdminController;
import setup.init;

import javax.swing.*;
import java.awt.*;

public class AdminLoginFrame extends JFrame {
    private JPasswordField secretPhraseField;
    private JLabel infoLabel;

    public AdminLoginFrame() {
        super("Validação do Administrador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 320));
        setup();
        pack();
        setLocationRelativeTo(null);
    }

    private void setup() {
        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(500);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Validação do Administrador"));
        header.add(UIUtils.createLabel(UIUtils.htmlWrap("Informe a frase secreta da chave privada do primeiro usuario cadastrado para liberar o sistema.", 440)));
        card.add(header, BorderLayout.NORTH);

        secretPhraseField = UIUtils.createPasswordField(28);
        JPanel form = UIUtils.createFormPanel();
        UIUtils.addFormRow(form, 0, "Frase secreta:", secretPhraseField);
        card.add(form, BorderLayout.CENTER);

        JButton submitBtn = UIUtils.createButton("Validar", UIUtils.COLOR_SUCCESS);
        submitBtn.addActionListener(e -> submitValidation());
        infoLabel = UIUtils.createLabel("");
        infoLabel.setForeground(UIUtils.COLOR_DANGER);

        JPanel footer = UIUtils.createWhitePanel(new BorderLayout(), 0);
        footer.add(UIUtils.createButtonRow(submitBtn), BorderLayout.NORTH);
        footer.add(infoLabel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        UIUtils.addCenteredCard(container, card);
        setContentPane(container);
    }

    private void submitValidation() {
        String secretPhrase = new String(secretPhraseField.getPassword());
        if (secretPhrase.isEmpty()) {
            infoLabel.setText("A frase secreta e obrigatória.");
            return;
        }

        String error = AdminController.validateAdmin(secretPhrase);
        if (error != null) {
            UIUtils.showError(this, "Validation Error", error);
            System.exit(1);
        }

        dispose();
        init.startAuth();
    }
}
