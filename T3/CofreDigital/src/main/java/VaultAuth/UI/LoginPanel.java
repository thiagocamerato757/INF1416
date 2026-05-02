package VaultAuth.UI;

import VaultAuth.AuthController;
import VaultAuth.LoginAuth;
import sun.rmi.runtime.Log;

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
            LoginAuth auth = LoginAuth.getInstance();
            auth.validateLogin(email.getText());

            email.setText("");

            AuthController ctrl = AuthController.getInstance();
            ctrl.Check();
        });
        add(proceed);
    }
}
