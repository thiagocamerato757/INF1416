package VaultAuth;

import VaultAuth.UI.AuthFrame;
import VaultAuth.UI.PassPanel;
import VaultAuth.UI.TOTPPanel;
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

    private AuthFrame frame;
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
                //frame.setPanel(null);
                frame.setTitle("AUTHORIZED");
                break;
            default:
                break;
        }
        System.out.println(authPhase);
    }

    private void resetAuth() {
        authPhase = AuthPhase.EMAIL;
        user = Optional.empty();
        NextPhase();
    }
}
