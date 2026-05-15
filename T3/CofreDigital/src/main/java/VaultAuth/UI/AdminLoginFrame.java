package VaultAuth.UI;

import setup.init;
import VaultAuth.AdminController;

import javax.swing.*;
import java.awt.*;

public class AdminLoginFrame extends JFrame {
    private JPasswordField secretPhraseField;
    private JLabel infoLabel;

    public AdminLoginFrame() {
        super("Validação do Administrador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 150);
        setLocationRelativeTo(null);
        setup();
    }

    private void setup() {
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel phraseRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel phraseLabel = new JLabel("Secret Phrase:");
        secretPhraseField = new JPasswordField(30);
        phraseRow.add(phraseLabel);
        phraseRow.add(secretPhraseField);
        add(phraseRow);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton submitBtn = new JButton("Validate");
        submitBtn.addActionListener(e -> submitValidation());
        buttonRow.add(submitBtn);
        add(buttonRow);

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoLabel = new JLabel("");
        infoLabel.setForeground(Color.RED);
        infoRow.add(infoLabel);
        add(infoRow);
    }

    private void submitValidation() {
        String secretPhrase = new String(secretPhraseField.getPassword());
        if (secretPhrase.isEmpty()) {
            infoLabel.setText("Secret phrase is required");
            return;
        }

        String error = AdminController.validateAdmin(secretPhrase);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        dispose();
        init.startAuth();
    }
}
