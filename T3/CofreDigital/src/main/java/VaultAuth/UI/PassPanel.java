package VaultAuth.UI;

import VaultAuth.AuthController;
import VaultAuth.PassAuth;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PassPanel extends JPanel {
    private JPasswordField passField;
    private JLabel info;
    private final JButton[] buttons = new JButton[5];
    private final List<Integer> values = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    private final List<Map.Entry<Integer, Integer>> entries = new ArrayList<>();
    private String passPlaceholder = "";

    public PassPanel() {
        super();
        setup();
    }

    private void setup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel passLabel = new JLabel("Password:");
        passField = new JPasswordField(passPlaceholder);
        passField.setColumns(20);
        passField.setEchoChar('*');
        passField.setEditable(false);
        topRow.add(passLabel);
        topRow.add(passField);

        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        Dimension buttonSize = new Dimension(70, 40);
        for (int i = 0; i < 5; i++) {
            JButton button = new JButton();
            button.setPreferredSize(buttonSize);
            button.setMinimumSize(buttonSize);
            button.setMaximumSize(buttonSize);
            buttons[i] = button;
            button.putClientProperty("index", i);
            button.addActionListener(e -> {
                int index = (int) ((JButton) e.getSource()).getClientProperty("index");
                passPlaceholder += "X";
                passField.setText(passPlaceholder);
                entries.add(new AbstractMap.SimpleEntry<>(values.get(index * 2), values.get(index * 2 + 1)));
                setupButtons();
            });
            buttonsRow.add(button);
        }

        JPanel proceedRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        proceedRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton proceed = new JButton("Proceed");
        proceed.addActionListener(e -> {
            passPlaceholder = "";
            passField.setText(passPlaceholder);
            PassAuth passAuth = PassAuth.getInstance();
            passAuth.validatePassword(passAuth.prepPasswords(entries));

            info.setText(passAuth.getFeedbackMessage());

            AuthController auth = AuthController.getInstance();
            auth.Check();
            entries.clear();
        });
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            passPlaceholder = "";
            passField.setText(passPlaceholder);
        });
        proceedRow.add(clear);
        proceedRow.add(proceed);

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        info = new JLabel("");
        infoRow.add(info);

        add(topRow);
        add(buttonsRow);
        add(proceedRow);
        add(infoRow);

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
