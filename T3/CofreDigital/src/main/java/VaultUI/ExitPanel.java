package VaultUI;

import UI.UIUtils;
import VaultAuth.AdminController;
import logger.Logger;
import model.UserModel;

import javax.swing.*;
import java.awt.*;

public class ExitPanel extends JPanel {
    private final UserModel user;
    private final Runnable onBack;
    private final Runnable onEndSession;

    public ExitPanel(UserModel user, Runnable onBack, Runnable onEndSession) {
        this.user = user;
        this.onBack = onBack;
        this.onEndSession = onEndSession;
        Logger.log(8001, user.getUid(), user.getLogin());
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);

        JPanel container = UIUtils.createCenteredContainer();
        JPanel card = UIUtils.createContentCard(660);

        JPanel header = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_SMALL), 0);
        header.add(UIUtils.createTitleLabel("Sair"));
        header.add(UIUtils.createLabel("Login: " + user.getLogin()));
        header.add(UIUtils.createLabel("Grupo: " + (user.getGrupoId() == 1 ? "Administrador" : "Usuario")));
        header.add(UIUtils.createLabel("Nome: " + user.getNome()));
        card.add(header, BorderLayout.NORTH);

        JPanel body = UIUtils.createWhitePanel(new GridLayout(0, 1, 0, UIUtils.PADDING_MEDIUM), 0);
        body.add(UIUtils.createSubtitleLabel("Total de Acessos: " + user.getTotalAcessos()));
        body.add(UIUtils.createLabel(UIUtils.htmlWrap("Pressione Encerrar Sessão para voltar a tela de autenticação, ou Encerrar Sistema para finalizar o Cofre Digital.", 580)));
        card.add(body, BorderLayout.CENTER);

        JButton session = UIUtils.createButton("Encerrar Sessão", UIUtils.COLOR_WARNING);
        session.addActionListener(e -> {
            Logger.log(8002, user.getUid(), user.getLogin());
            if (onEndSession != null) onEndSession.run();
        });
        JButton system = UIUtils.createButton("Encerrar Sistema", UIUtils.COLOR_DANGER);
        system.addActionListener(e -> {
            Logger.log(8003, user.getUid(), user.getLogin());
            Logger.log(1002, user.getUid(), user.getLogin());
            AdminController.clearAdminSecretPhrase();
            System.exit(0);
        });
        JButton back = UIUtils.createButton("Voltar", UIUtils.COLOR_ACCENT);
        back.addActionListener(e -> {
            Logger.log(8004, user.getUid(), user.getLogin());
            if (onBack != null) onBack.run();
        });
        card.add(UIUtils.createButtonRow(session, system, back), BorderLayout.SOUTH);

        UIUtils.addCenteredCard(container, card);
        add(container, BorderLayout.CENTER);
    }
}
