package VaultAuth.UI;

import VaultAuth.AuthController;
import VaultAuth.LoginAuth;

import javax.swing.*;

public class LoginPanel extends JPanel {
    private JTextField email;

    public LoginPanel() {
        super();
        setup();
    }

    private void setup() {
        JLabel emailLabel = new JLabel("Email:");
        add(emailLabel);
        email = new JTextField(30);
        add(email);
        JButton proceed = new JButton("Proceed");
        proceed.addActionListener(e -> {
            AuthController auth = AuthController.getInstance();
            auth.Check(email.getText().getBytes());
        });
        add(proceed);
    }
}
