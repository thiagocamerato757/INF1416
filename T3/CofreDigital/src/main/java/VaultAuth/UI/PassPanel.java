package VaultAuth.UI;

import VaultAuth.AuthController;
import VaultAuth.PassAuth;

import javax.swing.*;
import java.util.*;

public class PassPanel extends JPanel {
    private JPasswordField passField;
    private JButton proceed;
    private JButton[] buttons = new JButton[5];
    private List<Integer> values = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    private List<Map.Entry<Integer, Integer>> entries = new ArrayList<>();
    private String passPlaceholder = "";

    public PassPanel() {
        super();
        setup();
    }

    private void setup() {
        JLabel passLabel = new JLabel("Password:");
        add(passLabel);

        passField = new JPasswordField(passPlaceholder);
        passField.setColumns(20);
        passField.setEchoChar('*');
        passField.setEditable(false);
        add(passField);

        proceed = new JButton("Proceed");
        proceed.addActionListener(e -> {
            passPlaceholder = "";
            passField.setText(passPlaceholder);
            PassAuth passAuth = PassAuth.getInstance();
            passAuth.validatePassword(passAuth.prepPasswords(entries));

            AuthController auth = AuthController.getInstance();
            auth.Check();
            entries.clear();
        });
        add(proceed);

        for (int i = 0; i < 5; i++) {
            JButton button = new JButton();
            buttons[i] = button;
            button.putClientProperty("index", i);
            button.addActionListener(e -> {
                int index = (int) ((JButton) e.getSource()).getClientProperty("index");
                passPlaceholder += "X";
                passField.setText(passPlaceholder);
                entries.add(new AbstractMap.SimpleEntry<>(values.get(index * 2), values.get(index * 2 + 1)));
                setupButtons();
            });
            add(button);
        }

        setupButtons();
    }

    private void setupButtons() {
        Collections.shuffle(values);
        int index = 0;
        for (JButton button : buttons) {
            button.setText(values.get(index) + " or " + values.get(index + 1));
            index += 2;
        }
    }

}
