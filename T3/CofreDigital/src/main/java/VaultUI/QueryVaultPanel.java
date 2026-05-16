package VaultUI;

import UI.UIUtils;
import VaultAuth.AdminController;
import crypto.PasswordUtil;
import db.dao.KeyDAO;
import db.dao.UserDAO;
import logger.Logger;
import model.UserModel;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class QueryVaultPanel extends JPanel {
    private final UserModel user;
    private final Runnable onBack;
    private JTextField folderPath;
    private JPasswordField secretPhrase;
    private DefaultListModel<FileEntry> listModel;
    private JList<FileEntry> fileList;
    private JLabel info;
    private File currentFolder;

    public QueryVaultPanel(UserModel user, Runnable onBack) {
        this.user = user;
        this.onBack = onBack;
        Logger.log(7001, user.getUid());
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);

        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(860);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Consulta de Arquivos Secretos"));
        header.add(UIUtils.createLabel("Login: " + user.getLogin()));
        header.add(UIUtils.createLabel("Grupo: " + (user.getGrupoId() == 1 ? "Administrador" : "Usuario")));
        header.add(UIUtils.createLabel("Nome: " + user.getNome()));
        header.add(UIUtils.createSubtitleLabel("Total de Consultas: " + user.getTotalConsultas()));
        card.add(header, BorderLayout.NORTH);

        folderPath = UIUtils.createTextField(36);
        secretPhrase = UIUtils.createPasswordField(36);
        JPanel form = UIUtils.createFormPanel();
        UIUtils.addFormRow(form, 0, "Caminho da pasta:", UIUtils.createPathFieldWithButton(folderPath, UIUtils.createBrowseDirectoryButton(this, folderPath)));
        UIUtils.addFormRow(form, 1, "Frase secreta:", secretPhrase);

        JButton listButton = UIUtils.createButton("Listar", UIUtils.COLOR_PRIMARY);
        listButton.addActionListener(e -> listFiles());
        JButton decryptButton = UIUtils.createButton("Decriptar Selecionado", UIUtils.COLOR_SUCCESS);
        decryptButton.addActionListener(e -> decryptSelected());
        JButton backButton = UIUtils.createButton("Voltar", UIUtils.COLOR_ACCENT);
        backButton.addActionListener(e -> {
            Logger.log(7002, user.getUid());
            if (onBack != null) onBack.run();
        });
        UIUtils.addFormRow(form, 2, "Acoes:", UIUtils.createButtonRow(listButton, decryptButton, backButton));
        card.add(form, BorderLayout.CENTER);

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setVisibleRowCount(7);
        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && fileList.getSelectedValue() != null) {
                Logger.log(7010, user.getUid(), fileList.getSelectedValue().codeName);
            }
        });

        JPanel listPanel = UIUtils.createWhitePanel(new BorderLayout(UIUtils.PADDING_SMALL, UIUtils.PADDING_SMALL), 0);
        listPanel.add(UIUtils.createSubtitleLabel("Arquivos disponíveis"), BorderLayout.NORTH);
        listPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);
        info = UIUtils.createLabel("Selecione uma pasta e clique em Listar.");
        listPanel.add(info, BorderLayout.SOUTH);
        card.add(listPanel, BorderLayout.SOUTH);

        UIUtils.addCenteredCard(container, card);
        add(container, BorderLayout.CENTER);
    }
    private void listFiles() {
        Logger.log(7003, user.getUid());
        listModel.clear();
        currentFolder = new File(folderPath.getText().trim());
        if (!currentFolder.isDirectory()) {
            Logger.log(7004, user.getUid());
            UIUtils.showError(this, "Erro", "Caminho de pasta inválido.");
            return;
        }

        try {
            PrivateKey adminPrivateKey = loadStoredPrivateKey(UserDAO.getFirstUser(), AdminController.getAdminSecretPhrase());
            X509Certificate adminCert = loadStoredCertificate(UserDAO.getFirstUser());
            byte[] seed = decryptEnvelope(new File(currentFolder, "index.env"), adminPrivateKey);
            byte[] encryptedIndex = Files.readAllBytes(new File(currentFolder, "index.enc").toPath());
            byte[] indexBytes = decryptAes(encryptedIndex, seed);
            Logger.log(7005, user.getUid());

            byte[] signature = Files.readAllBytes(new File(currentFolder, "index.asd").toPath());
            if (verifySignature(indexBytes, signature, adminCert.getPublicKey())) {
                Logger.log(7008, user.getUid());
                UIUtils.showError(this, "Erro", "Assinatura do índice inválida.");
                return;
            }
            Logger.log(7006, user.getUid());

            List<FileEntry> entries = parseIndex(new String(indexBytes, StandardCharsets.UTF_8));
            for (FileEntry entry : entries) {
                if (canSee(entry)) listModel.addElement(entry);
            }
            user.setTotalConsultas(user.getTotalConsultas() + 1);
            UserDAO.updateUser(user);
            Logger.log(7009, user.getUid());
            info.setText(listModel.size() + " arquivo(s) disponível(is).");
        } catch (Exception e) {
            Logger.log(7007, user.getUid());
            UIUtils.showError(this, "Erro", "No foi possível abrir o índice: " + e.getMessage());
        }
    }

    private void decryptSelected() {
        FileEntry entry = fileList.getSelectedValue();
        if (entry == null) return;
        if (!isOwner(entry)) {
            Logger.log(7012, user.getUid(), entry.codeName);
            UIUtils.showWarning(this, "Acesso negado", "Você não pode abrir arquivos dos quais não é dono.");
            return;
        }
        Logger.log(7011, user.getUid(), entry.codeName);

        try {
            String phrase = new String(secretPhrase.getPassword());
            PrivateKey privateKey = loadStoredPrivateKey(user, phrase);
            X509Certificate cert = loadStoredCertificate(user);
            byte[] seed = decryptEnvelope(new File(currentFolder, entry.codeName + ".env"), privateKey);
            byte[] encryptedFile = Files.readAllBytes(new File(currentFolder, entry.codeName + ".enc").toPath());
            byte[] plain = decryptAes(encryptedFile, seed);
            Logger.log(7013, user.getUid(), entry.codeName);

            byte[] signature = Files.readAllBytes(new File(currentFolder, entry.codeName + ".asd").toPath());
            if (verifySignature(plain, signature, cert.getPublicKey())) {
                Logger.log(7016, user.getUid(), entry.codeName);
                UIUtils.showError(this, "Erro", "Assinatura do arquivo inválida.");
                return;
            }
            Logger.log(7014, user.getUid(), entry.codeName);

            File outDir = new File(currentFolder, "decrypted");
            if (!outDir.exists() && !outDir.mkdirs()) {
                throw new IllegalStateException("Não foi possível criar pasta decrypted");
            }
            Files.write(new File(outDir, entry.secretName).toPath(), plain);
            UIUtils.showSuccess(this, "Sucesso", "Arquivo decriptado em decrypted/" + entry.secretName);
        } catch (Exception e) {
            Logger.log(7015, user.getUid(), entry.codeName);
            UIUtils.showError(this, "Erro", "Falha ao decriptar arquivo: " + e.getMessage());
        }
    }

    private boolean canSee(FileEntry entry) {
        return isOwner(entry) || normalize(entry.group).equals(normalize(groupName(user)));
    }

    private boolean isOwner(FileEntry entry) {
        String owner = normalize(entry.owner);
        return owner.equals(normalize(user.getLogin())) || owner.equals(normalize(user.getNome()));
    }

    private String groupName(UserModel u) {
        return u.getGrupoId() == 1 ? "Administrador" : "UsuÃƒÂ¡rio";
    }

    private String normalize(String value) {
        if (value == null) return "";
        String v = value.trim().toLowerCase();
        if (v.equals("admin")) return "administrador";
        if (v.equals("user") || v.equals("usuario")) return "usuário";
        return v;
    }

    private List<FileEntry> parseIndex(String text) {
        List<FileEntry> entries = new ArrayList<>();
        for (String line : text.split("\\R")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+", 4);
            if (parts.length >= 4) entries.add(new FileEntry(parts[0], parts[1], parts[2], parts[3]));
        }
        return entries;
    }

    private PrivateKey loadStoredPrivateKey(UserModel owner, String phrase) throws Exception {
        if (owner == null || owner.getKid() == null) throw new IllegalStateException("Usuário sem chave cadastrada");
        Object[] certAndKey = KeyDAO.getCertAndKey(owner.getKid());
        if (certAndKey == null) throw new IllegalStateException("Chave não encontrada no banco");
        byte[] decrypted = PasswordUtil.decrypt((byte[]) certAndKey[1], phrase);
        return parsePrivateKey(decrypted, loadStoredCertificate(owner).getPublicKey().getAlgorithm());
    }

    private X509Certificate loadStoredCertificate(UserModel owner) throws Exception {
        Object[] certAndKey = KeyDAO.getCertAndKey(owner.getKid());
        if (certAndKey == null) throw new IllegalStateException("Certificado não encontrado no banco");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(((String) certAndKey[0]).getBytes(StandardCharsets.UTF_8)));
    }

    private PrivateKey parsePrivateKey(byte[] keyBytes, String algorithm) throws Exception {
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        try {
            return kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception ignored) {
            String text = new String(keyBytes, StandardCharsets.UTF_8).trim()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(text)));
        }
    }

    private byte[] decryptEnvelope(File envFile, PrivateKey privateKey) throws Exception {
        byte[] encryptedSeed = Files.readAllBytes(envFile.toPath());
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedSeed);
    }

    private byte[] decryptAes(byte[] encrypted, byte[] seed) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, deriveAesKey(seed));
        return cipher.doFinal(encrypted);
    }

    private SecretKey deriveAesKey(byte[] seed) throws Exception {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(seed);
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, random);
        return keyGen.generateKey();
    }

    private boolean verifySignature(byte[] data, byte[] sigBytes, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return !sig.verify(sigBytes);
    }

    private static class FileEntry {
        private final String codeName;
        private final String secretName;
        private final String owner;
        private final String group;

        private FileEntry(String codeName, String secretName, String owner, String group) {
            this.codeName = codeName;
            this.secretName = secretName;
            this.owner = owner;
            this.group = group;
        }

        public String toString() {
            return codeName + " -> " + secretName + " (dono: " + owner + ", grupo: " + group + ")";
        }
    }
}
