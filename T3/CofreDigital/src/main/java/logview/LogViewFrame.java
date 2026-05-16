package logview;

import UI.UIUtils;
import crypto.PasswordUtil;
import db.DataBaseStarter;
import db.dao.KeyDAO;
import db.dao.UserDAO;
import model.UserModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class LogViewFrame extends JFrame {

    private JTextField keyPathField;
    private JPasswordField secretPhraseField;
    private JLabel infoLabel;

    public LogViewFrame(String defaultKeyPath) {
        super("Auditoria - Cofre Digital");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(680, 420));
        setupAuthPanel(defaultKeyPath);
        pack();
        setLocationRelativeTo(null);
    }

    private void setupAuthPanel(String defaultKeyPath) {
        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(600);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Auditoria - logView"));
        header.add(UIUtils.createLabel(UIUtils.htmlWrap("Selecione a chave privada do administrador e informe a frase secreta para acessar os registros de auditoria.", 540)));
        card.add(header, BorderLayout.NORTH);

        keyPathField = UIUtils.createTextField(32);
        if (defaultKeyPath != null) {
            keyPathField.setText(defaultKeyPath);
        }
        secretPhraseField = UIUtils.createPasswordField(28);

        JPanel form = UIUtils.createFormPanel();
        UIUtils.addFormRow(form, 0, "Chave privada:", UIUtils.createPathFieldWithButton(keyPathField, UIUtils.createBrowseFileButton(this, keyPathField)));
        UIUtils.addFormRow(form, 1, "Frase secreta:", secretPhraseField);
        card.add(form, BorderLayout.CENTER);

        JButton submitBtn = UIUtils.createButton("Autenticar", UIUtils.COLOR_SUCCESS);
        submitBtn.addActionListener(e -> authenticate());
        infoLabel = UIUtils.createLabel("");
        infoLabel.setForeground(UIUtils.COLOR_DANGER);

        JPanel footer = UIUtils.createWhitePanel(new BorderLayout(), 0);
        footer.add(UIUtils.createButtonRow(submitBtn), BorderLayout.NORTH);
        footer.add(infoLabel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        UIUtils.addCenteredCard(container, card);
        setContentPane(container);
    }

    private void authenticate() {
        String keyPath = keyPathField.getText().trim();
        String secretPhrase = new String(secretPhraseField.getPassword());

        if (keyPath.isEmpty()) {
            infoLabel.setText("O caminho da chave privada e obrigatorio.");
            return;
        }
        if (secretPhrase.isEmpty()) {
            infoLabel.setText("A frase secreta e obrigatoria.");
            return;
        }

        if (!Files.exists(Paths.get(keyPath))) {
            infoLabel.setText("Arquivo de chave privada nao encontrado.");
            return;
        }

        try {
            doAuthenticate(keyPath, secretPhrase);
        } catch (Exception e) {
            UIUtils.showError(this, "Falha na autenticacao", e.getMessage());
            System.exit(1);
        }

        showLogs();
    }

    private void doAuthenticate(String keyPath, String secretPhrase) throws Exception {
        UserModel admin = UserDAO.getFirstUser();
        if (admin == null) {
            throw new Exception("Nenhum administrador encontrado no banco de dados");
        }

        Integer kid = admin.getKid();
        if (kid == null) {
            throw new Exception("Administrador nao possui par de chaves registrado");
        }

        Object[] certAndKey = KeyDAO.getCertAndKey(kid);
        if (certAndKey == null) {
            throw new Exception("Falha ao recuperar par de chaves do administrador");
        }

        String certPem = (String) certAndKey[0];

        byte[] encryptedKeyBytes = Files.readAllBytes(Paths.get(keyPath));
        byte[] decryptedBytes = PasswordUtil.decrypt(encryptedKeyBytes, secretPhrase);

        String base64Key = new String(decryptedBytes, StandardCharsets.UTF_8).trim();
        if (base64Key.contains("-----BEGIN")) {
            StringBuilder sb = new StringBuilder();
            for (String line : base64Key.split("\n")) {
                line = line.trim();
                if (!line.startsWith("-----")) {
                    sb.append(line);
                }
            }
            base64Key = sb.toString();
        } else {
            base64Key = base64Key.replaceAll("\\s+", "");
        }

        byte[] pkcs8Bytes = Base64.getDecoder().decode(base64Key);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8Bytes));

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(certPem.getBytes(StandardCharsets.UTF_8)));

        SecureRandom random = new SecureRandom();
        byte[] data = new byte[2048];
        random.nextBytes(data);

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(data);
        byte[] signature = signer.sign();

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(cert.getPublicKey());
        verifier.update(data);
        if (!verifier.verify(signature)) {
            throw new Exception("Chave privada nao corresponde ao certificado");
        }
    }

    private void showLogs() {
        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(900);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Registros de Auditoria"));
        card.add(header, BorderLayout.NORTH);

        String[] columns = {"Data/Hora", "MID", "UID", "Arquivo", "Mensagem"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        String sql = "SELECT r.data_hora, r.MID, r.UID, r.fname, m.texto, u.login " +
                     "FROM Registros r " +
                     "JOIN Mensagens m ON r.MID = m.MID " +
                     "LEFT JOIN Usuarios u ON r.UID = u.UID " +
                     "ORDER BY r.data_hora ASC, r.RID ASC";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = DataBaseStarter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("data_hora");
                int mid = rs.getInt("MID");
                int uid = rs.getInt("UID");
                String uidStr = rs.wasNull() ? "-" : String.valueOf(uid);
                String fname = rs.getString("fname");
                String texto = rs.getString("texto");
                String login = rs.getString("login");

                String msg;
                int p = countPlaceholders(texto);
                if (p == 0) {
                    msg = texto;
                } else if (p == 1) {
                    msg = String.format(texto, login != null ? login : "(?)");
                } else {
                    msg = String.format(texto,
                            fname != null ? fname : "(?)",
                            login != null ? login : "(?)");
                }

                model.addRow(new Object[]{
                        sdf.format(new Date(ts.getTime())),
                        String.format("%04d", mid),
                        uidStr,
                        fname != null ? fname : "",
                        msg
                });
            }
        } catch (Exception e) {
            UIUtils.showError(this, "Erro", "Erro ao carregar logs: " + e.getMessage());
            System.exit(1);
        }

        JTable table = new JTable(model);
        table.setFont(UIUtils.FONT_BODY);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(500);
        table.getTableHeader().setFont(UIUtils.FONT_SUBTITLE);
        table.getTableHeader().setBackground(UIUtils.COLOR_PRIMARY);
        table.getTableHeader().setForeground(UIUtils.COLOR_LIGHT);
        table.setSelectionBackground(UIUtils.COLOR_PRIMARY);
        table.setSelectionForeground(UIUtils.COLOR_LIGHT);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = UIUtils.createButton("Fechar", UIUtils.COLOR_DANGER);
        closeBtn.addActionListener(e -> System.exit(0));
        card.add(UIUtils.createButtonRow(closeBtn), BorderLayout.SOUTH);

        UIUtils.addCenteredCard(container, card);
        setContentPane(container);
        setMinimumSize(new Dimension(960, 540));
        pack();
        setLocationRelativeTo(null);
    }

    private static int countPlaceholders(String s) {
        int count = 0;
        int idx = 0;
        while ((idx = s.indexOf("%s", idx)) != -1) {
            count++;
            idx += 2;
        }
        return count;
    }
}
