package VaultUI;

import UI.UIUtils;
import logger.Logger;
import model.UserModel;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    private final UserModel currentUser;
    private Runnable onRegisterUser;
    private Runnable onQueryVault;
    private Runnable onExit;

    public MenuPanel(UserModel user) {
        this.currentUser = user;
        Logger.log(5001, user.getUid());
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);

        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(620);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Cofre Digital"));
        header.add(UIUtils.createLabel("Login: " + currentUser.getLogin()));
        header.add(UIUtils.createLabel("Grupo: " + groupName()));
        header.add(UIUtils.createLabel("Nome: " + currentUser.getNome()));
        card.add(header, BorderLayout.NORTH);

        JPanel body = UIUtils.createWhitePanel(new BorderLayout(UIUtils.PADDING_MEDIUM, UIUtils.PADDING_MEDIUM), 0);
        body.add(UIUtils.createSubtitleLabel("Total de Acessos: " + currentUser.getTotalAcessos()), BorderLayout.NORTH);

        JPanel options = UIUtils.createPanel(new GridLayout(0, 1, 0, UIUtils.PADDING_MEDIUM), 0);
        options.setOpaque(false);
        if (currentUser.getGrupoId() == 1) {
            JButton registerBtn = UIUtils.createButton("1. Cadastrar Novo Usuario", UIUtils.COLOR_PRIMARY);
            registerBtn.addActionListener(e -> handleRegisterUser());
            options.add(registerBtn);
        }
        JButton queryBtn = UIUtils.createButton("2. Consultar Pasta de Arquivos", UIUtils.COLOR_PRIMARY);
        queryBtn.addActionListener(e -> handleQueryVault());
        options.add(queryBtn);

        JButton exitBtn = UIUtils.createButton("3. Sair", UIUtils.COLOR_DANGER);
        exitBtn.addActionListener(e -> handleExit());
        options.add(exitBtn);
        body.add(options, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        UIUtils.addCenteredCard(container, card);
        add(container, BorderLayout.CENTER);
    }

    private String groupName() {
        return currentUser.getGrupoId() == 1 ? "Administrador" : "Usuario";
    }

    private void handleRegisterUser() {
        Logger.log(5002, currentUser.getUid());
        if (onRegisterUser != null) onRegisterUser.run();
    }

    private void handleQueryVault() {
        Logger.log(5003, currentUser.getUid());
        if (onQueryVault != null) onQueryVault.run();
    }

    private void handleExit() {
        Logger.log(5004, currentUser.getUid());
        if (onExit != null) onExit.run();
    }

    public void setOnRegisterUser(Runnable callback) {
        this.onRegisterUser = callback;
    }

    public void setOnQueryVault(Runnable callback) {
        this.onQueryVault = callback;
    }

    public void setOnExit(Runnable callback) {
        this.onExit = callback;
    }

}
