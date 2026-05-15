package VaultAuth.UI;

import crypto.PasswordUtil;
import setup.init;
import VaultAuth.AdminController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.swing.*;
import java.awt.*;
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setup();
    }

    private void setup() {
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        certField = new JTextField(40);
        keyField = new JTextField(40);
        secretPhraseField = new JPasswordField(40);
        passwordField = new JPasswordField(40);
        confirmPasswordField = new JPasswordField(40);
        infoLabel = new JLabel("");

        add(createFileRow("Certificate (PEM):", certField, this::browseCert));
        add(createFileRow("Private Key:", keyField, this::browseKey));
        add(createFieldRow("Secret Phrase:", secretPhraseField));
        add(createFieldRow("Personal Password:", passwordField));
        add(createFieldRow("Confirm Password:", confirmPasswordField));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton submitBtn = new JButton("Register");
        submitBtn.addActionListener(e -> submitSetup());
        buttonRow.add(submitBtn);
        add(buttonRow);

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoLabel.setForeground(Color.RED);
        infoRow.add(infoLabel);
        add(infoRow);
    }

    private JPanel createFileRow(String label, JTextField field, Runnable browseAction) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(120, 25));
        row.add(lbl);
        row.add(field);
        JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(e -> browseAction.run());
        row.add(browseBtn);
        return row;
    }

    private JPanel createFieldRow(String label, JPasswordField field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(120, 25));
        row.add(lbl);
        field.setPreferredSize(new Dimension(300, 25));
        row.add(field);
        return row;
    }

    private void browseCert() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            certField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void browseKey() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            keyField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void submitSetup() {
        String certPath = certField.getText().trim();
        String keyPath = keyField.getText().trim();
        String secretPhrase = new String(secretPhraseField.getPassword());
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (certPath.isEmpty() || keyPath.isEmpty() || secretPhrase.isEmpty() || password.isEmpty()) {
            infoLabel.setText("All fields are required");
            return;
        }

        if (!PasswordUtil.isValidPersonalPassword(password)) {
            infoLabel.setText("Personal password is not valid, use from 8 to 10 digits and no repeating characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            infoLabel.setText("Passwords do not match");
            return;
        }

        String error = AdminController.setupAdmin(certPath, keyPath, secretPhrase, password);
        if (error != null) {
            infoLabel.setText(error);
            return;
        }

        String totpSecret = AdminController.getLastGeneratedTotpSecret();
        showTotpDialog(totpSecret);

        dispose();
        init.startAuth();
    }

    private void showTotpDialog(String totpSecret) {
        JDialog dialog = new JDialog(this, "TOTP Secret", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));

        JLabel textLabel = new JLabel("Secret key (BASE32): " + totpSecret);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dialog.add(textLabel);

        try {
            String uri = "otpauth://totp/CofreDigital:admin?secret=" + totpSecret + "&issuer=CofreDigital";
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(uri, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            byte[] qrBytes = baos.toByteArray();
            ImageIcon qrIcon = new ImageIcon(qrBytes);
            JLabel qrLabel = new JLabel(qrIcon);
            qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            dialog.add(qrLabel);
        } catch (Exception e) {
            JLabel fallback = new JLabel("QR Code generation failed");
            fallback.setAlignmentX(Component.CENTER_ALIGNMENT);
            dialog.add(fallback);
        }

        JButton okBtn = new JButton("OK");
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.addActionListener(e -> dialog.dispose());
        dialog.add(okBtn);

        dialog.setVisible(true);
    }
}
