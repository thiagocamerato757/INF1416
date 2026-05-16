package VaultAuth;

import VaultAuth.UI.AuthFrame;
import VaultAuth.UI.LoginPanel;
import VaultAuth.UI.PassPanel;
import VaultAuth.UI.TOTPPanel;
import VaultUI.ExitPanel;
import VaultUI.MenuPanel;
import VaultUI.QueryVaultPanel;
import VaultUI.UserRegPanel;
import db.dao.UserDAO;
import logger.Logger;
import model.UserModel;

import javax.swing.*;
import java.util.Optional;

enum AuthPhase {
    EMAIL,
    PASSWORD,
    TOTP,
    AUTHORIZED
}

public class AuthController {
    private static AuthPhase authPhase = AuthPhase.EMAIL;
    private static final AuthController instance = new AuthController();

    private final AuthFrame frame;
    private Optional<UserModel> user = Optional.empty();

    private AuthController() {
        frame = new AuthFrame("Authentication");
    }

    public static AuthController getInstance() {
        return instance;
    }

    public AuthFrame getAuthFrame() {
        return frame;
    }

    public void Check() {
        switch (authPhase) {
            case EMAIL:
                LoginAuth loginAuth = LoginAuth.getInstance();
                if (loginAuth.isValidated()) {
                    user = Optional.of(loginAuth.getUser());
                    NextPhase();
                }
                break;
            case PASSWORD:
                PassAuth passAuth = PassAuth.getInstance();
                if (passAuth.isValidated()) {
                    NextPhase();
                }
                break;
            case TOTP:
                TOTP totp = TOTP.getInstance();
                if (totp.isValidated()) {
                    NextPhase();
                }
                break;
            default:
                break;
        }
    }

    private void NextPhase() {
        switch (authPhase) {
            case EMAIL:
                authPhase = AuthPhase.PASSWORD;
                frame.setPanel(new PassPanel());
                break;
            case PASSWORD:
                authPhase = AuthPhase.TOTP;
                frame.setPanel(new TOTPPanel());
                break;
            case TOTP:
                authPhase = AuthPhase.AUTHORIZED;
                openMainMenuAfterSuccessfulAuth();
                break;
            default:
                break;
        }
    }

    private void openMainMenuAfterSuccessfulAuth() {
        if (!user.isPresent()) return;
        UserModel current = user.get();
        current.setTotalAcessos(current.getTotalAcessos() + 1);
        current.setErroSenha(0);
        current.setErroToken(0);
        current.setBloqueadoAte(null);
        UserDAO.updateUser(current);
        Logger.log(1003, current.getUid());
        showMenu();
    }

    public void showMenu() {
        if (!user.isPresent()) return;
        MenuPanel menu = new MenuPanel(user.get());
        menu.setOnRegisterUser(() -> {
            UserRegPanel reg = new UserRegPanel(user.get());
            reg.setOnBack(this::showMenu);
            reg.setOnSuccess(this::showMenu);
            frame.setPanel(reg);
        });
        menu.setOnQueryVault(() -> frame.setPanel(new QueryVaultPanel(user.get(), this::showMenu)));
        menu.setOnExit(() -> frame.setPanel(new ExitPanel(user.get(), this::showMenu, this::resetAuth)));
        frame.setTitle("Cofre Digital");
        frame.setPanel(menu);
    }

    public Optional<UserModel> getUser() {
        return user;
    }

    public void resetAuth() {
        user.ifPresent(u -> Logger.log(1004, u.getUid()));
        restartAuthentication();
    }

    public void restartAuthentication() {
        LoginAuth.getInstance().ResetAuth();
        PassAuth.getInstance().ResetAuth();
        TOTP.getInstance().ResetAuth();
        authPhase = AuthPhase.EMAIL;
        user = Optional.empty();
        frame.setTitle("Authentication");
        frame.setPanel(new LoginPanel());
    }
}
