package VaultAuth.UI;

import VaultAuth.AuthController;
import VaultAuth.LoginAuth;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField email;
    private JLabel info;

    public LoginPanel() {
        super();
        setup();
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel emailRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel emailLabel = new JLabel("Email:");
        email = new JTextField(30);
        emailRow.add(emailLabel);
        emailRow.add(email);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton proceed = new JButton("Proceed");
        proceed.addActionListener(e -> {
            LoginAuth auth = LoginAuth.getInstance();
            auth.validateLogin(email.getText());

            email.setText("");
            info.setText(auth.getFeedbackMessage());

            AuthController ctrl = AuthController.getInstance();
            ctrl.Check();
        });
        buttonRow.add(proceed);

        info = new JLabel("");
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoRow.add(info);

        emailRow.setPreferredSize(new Dimension(400, 40));
        buttonRow.setPreferredSize(new Dimension(400, 40));
        infoRow.setPreferredSize(new Dimension(400, 30));

        add(emailRow);
        add(buttonRow);
        add(infoRow);
    }
}
