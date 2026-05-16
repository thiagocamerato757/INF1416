package VaultAuth.UI;

import UI.UIUtils;
import VaultAuth.AuthController;
import VaultAuth.PassAuth;
import logger.Logger;

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
        super(new BorderLayout());
        AuthController.getInstance().getUser().ifPresent(u -> Logger.log(3001, u.getUid(), u.getLogin()));
        setup();
    }

    private void setup() {
        setBackground(UIUtils.COLOR_BACKGROUND);
        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(560);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Senha Pessoal"));
        header.add(UIUtils.createLabel("Use o teclado virtual numérico para informar sua senha."));
        card.add(header, BorderLayout.NORTH);

        passField = UIUtils.createPasswordField(18);
        passField.setEchoChar('*');
        passField.setEditable(false);
        JPanel form = UIUtils.createFormPanel();
        UIUtils.addFormRow(form, 0, "Senha:", passField);

        JPanel buttonsRow = UIUtils.createPanel(new GridLayout(1, 5, UIUtils.PADDING_SMALL, 0), UIUtils.PADDING_SMALL);
        buttonsRow.setOpaque(false);
        for (int i = 0; i < 5; i++) {
            JButton button = UIUtils.createButton("", UIUtils.COLOR_PRIMARY);
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
        UIUtils.addFormRow(form, 1, "Teclado:", buttonsRow);
        card.add(form, BorderLayout.CENTER);

        JButton clear = UIUtils.createButton("Limpar", UIUtils.COLOR_ACCENT);
        clear.addActionListener(e -> clearPassword());
        JButton proceed = UIUtils.createButton("Prosseguir", UIUtils.COLOR_SUCCESS);
        proceed.addActionListener(e -> {
            passPlaceholder = "";
            passField.setText(passPlaceholder);
            PassAuth passAuth = PassAuth.getInstance();
            passAuth.validatePassword(passAuth.prepPasswords(entries));
            info.setText(passAuth.getFeedbackMessage());
            AuthController.getInstance().Check();
            entries.clear();
        });
        info = UIUtils.createLabel("");
        info.setForeground(UIUtils.COLOR_DANGER);
        JPanel footer = UIUtils.createWhitePanel(new BorderLayout(), 0);
        footer.add(UIUtils.createButtonRow(clear, proceed), BorderLayout.NORTH);
        footer.add(info, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        setupButtons();
        UIUtils.addCenteredCard(container, card);
        add(container, BorderLayout.CENTER);
    }

    private void clearPassword() {
        passPlaceholder = "";
        passField.setText(passPlaceholder);
        entries.clear();
    }

    private void setupButtons() {
        Collections.shuffle(values);
        int index = 0;
        for (JButton button : buttons) {
            button.setText(values.get(index) + " ou " + values.get(index + 1));
            index += 2;
        }
    }
}
