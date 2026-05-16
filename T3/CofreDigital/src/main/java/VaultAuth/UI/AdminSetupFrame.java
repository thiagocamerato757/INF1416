package VaultAuth.UI;

import UI.UIUtils;
import VaultAuth.AdminController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import crypto.PasswordUtil;
import logger.Logger;
import setup.init;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;

public class AdminSetupFrame extends JFrame {
    private JTextField certField;
    private JTextField keyField;
    private JPasswordField secretPhraseField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel infoLabel;

    public AdminSetupFrame() {
        super("Cadastro do Administrador");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Logger.log(1002);
                System.exit(0);
            }
        });
        setMinimumSize(new Dimension(680, 460));
        setup();
        pack();
        setLocationRelativeTo(null);
    }

    private void setup() {
        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(620);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Cadastro Inicial do Administrador"));
        header.add(UIUtils.createLabel(UIUtils.htmlWrap("Selecione o certificado, a chave privada e informe as credenciais iniciais do cofre.", 560)));
        card.add(header, BorderLayout.NORTH);

        certField = UIUtils.createTextField(32);
        keyField = UIUtils.createTextField(32);
        secretPhraseField = UIUtils.createPasswordField(32);
        passwordField = UIUtils.createPasswordField(16);
        confirmPasswordField = UIUtils.createPasswordField(16);

        JPanel form = UIUtils.createFormPanel();
        UIUtils.addFormRow(form, 0, "Certificado (PEM):", UIUtils.createPathFieldWithButton(certField, UIUtils.createBrowseFileButton(this, certField)));
        UIUtils.addFormRow(form, 1, "Chave privada:", UIUtils.createPathFieldWithButton(keyField, UIUtils.createBrowseFileButton(this, keyField)));
        UIUtils.addFormRow(form, 2, "Frase secreta:", secretPhraseField);
        UIUtils.addFormRow(form, 3, "Senha pessoal:", passwordField);
        UIUtils.addFormRow(form, 4, "Confirmar senha:", confirmPasswordField);
        card.add(form, BorderLayout.CENTER);

        JButton submitBtn = UIUtils.createButton("Cadastrar", UIUtils.COLOR_SUCCESS);
        submitBtn.addActionListener(e -> submitSetup());
        infoLabel = UIUtils.createLabel("");
        infoLabel.setForeground(UIUtils.COLOR_DANGER);

        JPanel footer = UIUtils.createWhitePanel(new BorderLayout(), 0);
        footer.add(UIUtils.createButtonRow(submitBtn), BorderLayout.NORTH);
        footer.add(infoLabel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        UIUtils.addCenteredCard(container, card);
        setContentPane(container);
    }

    private void submitSetup() {
        String certPath = certField.getText().trim();
        String keyPath = keyField.getText().trim();
        String secretPhrase = new String(secretPhraseField.getPassword());
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (certPath.isEmpty() || keyPath.isEmpty() || secretPhrase.isEmpty() || password.isEmpty()) {
            infoLabel.setText("Todos os campos sao obrigatorios.");
            return;
        }
        if (!PasswordUtil.isValidPersonalPassword(password)) {
            infoLabel.setText("A senha deve ter 8 a 10 digitos e nao pode repetir todos os numeros.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            infoLabel.setText("As senhas nao coincidem.");
            return;
        }

        String error = AdminController.setupAdmin(certPath, keyPath, secretPhrase, password);
        if (error != null) {
            infoLabel.setText(UIUtils.htmlWrap(error, 560));
            return;
        }

        showTotpDialog(AdminController.getLastGeneratedTotpSecret());
        dispose();
        init.startAuth();
    }

    private void showTotpDialog(String totpSecret) {
        JDialog dialog = new JDialog(this, "TOTP Secret", true);
        dialog.setMinimumSize(new Dimension(440, 430));
        dialog.setLayout(new BorderLayout(UIUtils.PADDING_MEDIUM, UIUtils.PADDING_MEDIUM));

        JTextArea secret = UIUtils.createTextArea(2, 32);
        secret.setText(totpSecret);
        secret.setEditable(false);
        JPanel top = UIUtils.createWhitePanel(new BorderLayout(UIUtils.PADDING_SMALL, UIUtils.PADDING_SMALL), UIUtils.PADDING_LARGE);
        top.add(UIUtils.createLabel("Secret key (BASE32):"), BorderLayout.NORTH);
        top.add(new JScrollPane(secret), BorderLayout.CENTER);
        dialog.add(top, BorderLayout.NORTH);

        try {
            String uri = "otpauth://totp/CofreDigital:admin?secret=" + totpSecret + "&issuer=CofreDigital";
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(uri, BarcodeFormat.QR_CODE, 220, 220);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            JLabel qrLabel = new JLabel(new ImageIcon(baos.toByteArray()));
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dialog.add(qrLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            dialog.add(UIUtils.createLabel("QR Code generation failed"), BorderLayout.CENTER);
        }

        JButton okBtn = UIUtils.createButton("OK", UIUtils.COLOR_SUCCESS);
        okBtn.addActionListener(e -> dialog.dispose());
        dialog.add(UIUtils.createButtonRow(okBtn), BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
