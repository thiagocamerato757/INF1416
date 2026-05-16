package VaultUI;

import Cryptography.Base32;
import UI.UIUtils;
import crypto.KeyValidator;
import crypto.PasswordUtil;
import db.dao.UserDAO;
import logger.Logger;
import model.UserModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.io.ByteArrayOutputStream;

public class UserRegPanel extends JPanel {
    private JTextField certificatePath;
    private JTextField privateKeyPath;
    private JPasswordField passphrase;
    private JComboBox<String> groupCombo;
    private JPasswordField password;
    private JPasswordField confirmPassword;

    private final UserModel adminUser;
    private Runnable onSuccess;
    private Runnable onBack;

    public UserRegPanel(UserModel admin) {
        this.adminUser = admin;
        Logger.log(6001, admin.getUid());
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);

        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(760);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Cadastro de Novo Usuário"));
        header.add(UIUtils.createLabel("Administrador: " + adminUser.getNome()));
        header.add(UIUtils.createLabel("Total de usuários do sistema: " + UserDAO.countUsers()));
        card.add(header, BorderLayout.NORTH);

        certificatePath = UIUtils.createTextField(34);
        privateKeyPath = UIUtils.createTextField(34);
        passphrase = UIUtils.createPasswordField(34);
        groupCombo = UIUtils.createComboBox(new String[]{"Administrador", "Usuário"});
        password = UIUtils.createPasswordField(16);
        confirmPassword = UIUtils.createPasswordField(16);

        JPanel form = UIUtils.createFormPanel();
        UIUtils.addFormRow(form, 0, "Certificado digital:", UIUtils.createPathFieldWithButton(certificatePath, UIUtils.createBrowseFileButton(this, certificatePath)));
        UIUtils.addFormRow(form, 1, "Chave privada:", UIUtils.createPathFieldWithButton(privateKeyPath, UIUtils.createBrowseFileButton(this, privateKeyPath)));
        UIUtils.addFormRow(form, 2, "Frase secreta:", passphrase);
        UIUtils.addFormRow(form, 3, "Grupo:", groupCombo);
        UIUtils.addFormRow(form, 4, "Senha pessoal:", password);
        UIUtils.addFormRow(form, 5, "Confirmar senha:", confirmPassword);
        card.add(form, BorderLayout.CENTER);

        JButton registerBtn = UIUtils.createButton("Cadastrar", UIUtils.COLOR_SUCCESS);
        registerBtn.addActionListener(e -> handleRegister());
        JButton backBtn = UIUtils.createButton("Voltar", UIUtils.COLOR_ACCENT);
        backBtn.addActionListener(e -> handleBack());
        card.add(UIUtils.createButtonRow(registerBtn, backBtn), BorderLayout.SOUTH);

        UIUtils.addCenteredCard(container, card);
        add(container, BorderLayout.CENTER);
    }

    private void handleRegister() {
        Logger.log(6002, adminUser.getUid());

        String certPath = certificatePath.getText().trim();
        String keyPath = privateKeyPath.getText().trim();
        String secretPhrase = new String(passphrase.getPassword());
        int groupId = groupCombo.getSelectedIndex() + 1;
        String pwd = new String(password.getPassword());
        String confirmPwd = new String(confirmPassword.getPassword());

        try {
            if (!PasswordUtil.isValidPersonalPassword(pwd)) {
                Logger.log(6003, adminUser.getUid());
                UIUtils.showError(this, "Erro", "Senha deve ter 8-10 dígitos, apenas números, e não pode repetir todos os dígitos.");
                return;
            }
            if (!pwd.equals(confirmPwd)) {
                UIUtils.showError(this, "Erro", "As senhas não coincidem.");
                return;
            }

            java.security.cert.X509Certificate cert;
            try {
                cert = KeyValidator.loadCertificate(certPath);
            } catch (Exception e) {
                Logger.log(6004, adminUser.getUid());
                UIUtils.showError(this, "Erro", "Caminho ou formato do certificado inválido: " + e.getMessage());
                return;
            }

            java.security.PrivateKey privKey;
            try {
                privKey = KeyValidator.loadPrivateKey(keyPath, secretPhrase, cert);
            } catch (Exception e) {
                Logger.log(6005, adminUser.getUid());
                UIUtils.showError(this, "Erro", "Caminho da chave privada inválido ou frase secreta incorreta.");
                return;
            }

            if (KeyValidator.validateKeyPair(cert, privKey)) {
                Logger.log(6007, adminUser.getUid());
                UIUtils.showError(this, "Erro", "Chave privada não corresponde ao certificado.");
                return;
            }

            String subject = cert.getSubjectDN().getName();
            String email = extractEmail(subject);
            String name = extractName(subject);

            boolean confirm = UIUtils.showConfirmation(this,
                    "Confirmação de Dados",
                    "Confirmar dados do certificado:\n" +
                            "Nome: " + name + "\n" +
                            "E-mail: " + email + "\n" +
                            "Versao: " + cert.getVersion() + "\n" +
                            "Serie: " + cert.getSerialNumber() + "\n" +
                            "Assinatura: " + cert.getSigAlgName() + "\n" +
                            "Emissor: " + cert.getIssuerDN().getName() + "\n" +
                            "Validade: " + cert.getNotAfter());
            if (!confirm) {
                Logger.log(6009, adminUser.getUid());
                return;
            }
            Logger.log(6008, adminUser.getUid());

            if (UserDAO.getUserByLogin(email) != null) {
                UIUtils.showError(this, "Erro", "Usuário já cadastrado com este e-mail.");
                return;
            }

            byte[] totpSecret = new byte[20];
            new SecureRandom().nextBytes(totpSecret);
            Base32 b32 = new Base32(Base32.Alphabet.BASE32, false, false);
            String totpBase32 = b32.toString(totpSecret);
            showTOTPConfiguration(totpBase32, email);

            String hashedPassword = PasswordUtil.hashPassword(pwd);
            byte[] totpEncrypted = PasswordUtil.encrypt(totpBase32.getBytes(StandardCharsets.UTF_8), pwd);
            String certPem = new String(Files.readAllBytes(Paths.get(certPath)), StandardCharsets.UTF_8);
            byte[] keyEncrypted = Files.readAllBytes(Paths.get(keyPath));

            int kid = UserDAO.createKeypair(certPem, keyEncrypted);
            UserModel newUser = new UserModel();
            newUser.setLogin(email);
            newUser.setNome(name);
            newUser.setSenhaBcrypt(hashedPassword);
            newUser.setTotpSecretEncrypted(totpEncrypted);
            newUser.setGrupoId(groupId);
            newUser.setKid(kid);

            int uid = UserDAO.createUser(newUser);
            UserDAO.updateKeypairUID(kid, uid);
            clearForm();
            UIUtils.showSuccess(this, "Sucesso", "Usuário cadastrado com sucesso!");
            if (onSuccess != null) onSuccess.run();
        } catch (Exception e) {
            UIUtils.showError(this, "Erro", "Erro ao realizar cadastro: " + e.getMessage());
        }
    }

    private String extractEmail(String subject) {
        if (subject.contains("E=")) return UserDAO.normalizeLogin(subject.split("E=")[1].split(",")[0]);
        if (subject.contains("EMAILADDRESS=")) return UserDAO.normalizeLogin(subject.split("EMAILADDRESS=")[1].split(",")[0]);
        return UserDAO.normalizeLogin(subject);
    }

    private String extractName(String subject) {
        if (subject.contains("CN=")) return subject.split("CN=")[1].split(",")[0].trim();
        return "User";
    }

    private void showTOTPConfiguration(String totpBase32, String email) {
        JPanel totpPanel = UIUtils.createWhitePanel(new BorderLayout(UIUtils.PADDING_SMALL, UIUtils.PADDING_SMALL), UIUtils.PADDING_LARGE);
        totpPanel.add(UIUtils.createLabel("Configure o Google Authenticator com o código ou QR Code abaixo:"), BorderLayout.NORTH);

        JPanel content = UIUtils.createWhitePanel(new BorderLayout(UIUtils.PADDING_MEDIUM, UIUtils.PADDING_MEDIUM), 0);
        JTextArea secretArea = UIUtils.createTextArea(2, 40);
        secretArea.setText(totpBase32);
        secretArea.setEditable(false);
        content.add(new JScrollPane(secretArea), BorderLayout.NORTH);

        try {
            String uri = "otpauth://totp/CofreDigital:" + email + "?secret=" + totpBase32 + "&issuer=CofreDigital";
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(uri, BarcodeFormat.QR_CODE, 220, 220);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            JLabel qrLabel = new JLabel(new ImageIcon(baos.toByteArray()));
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            content.add(qrLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            content.add(UIUtils.createLabel("Não foi possível gerar o QR Code."), BorderLayout.CENTER);
        }

        totpPanel.add(content, BorderLayout.CENTER);
        UIUtils.showComponentInfo(this, "Segredo TOTP", totpPanel);
    }

    private void clearForm() {
        certificatePath.setText("");
        privateKeyPath.setText("");
        passphrase.setText("");
        password.setText("");
        confirmPassword.setText("");
        groupCombo.setSelectedIndex(1);
    }

    private void handleBack() {
        Logger.log(6010, adminUser.getUid());
        if (onBack != null) onBack.run();
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setOnBack(Runnable callback) {
        this.onBack = callback;
    }
}
