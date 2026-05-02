package VaultAuth;

import VaultAuth.UI.AuthFrame;
import VaultAuth.UI.PassPanel;
import VaultAuth.UI.TOTPPanel;

import javax.swing.*;

enum AuthPhase {
    EMAIL,
    PASSWORD,
    TOTP,
    AUTHORIZED
}

public class AuthController {
    private static AuthPhase authPhase = AuthPhase.EMAIL;

    private static AuthController instance = new AuthController();

    private AuthFrame frame;

    private AuthController() {
        frame = new AuthFrame("Authentication");
    }

    public static AuthController getInstance() {
        return instance;
    }

    public AuthFrame getAuthFrame() {
        return frame;
    }

    public void Check(byte[] data) {
        switch (authPhase) {
            case EMAIL:
                String email = new String(data);
                LoginAuth loginAuth = new LoginAuth();
                if (loginAuth.validateLogin(email)) {
                    NextPhase();
                }
                break;
            case PASSWORD:
                break;
            case TOTP:
                break;
        }
    }

    public void NextPhase() {
        switch (authPhase) {
            case EMAIL:
                authPhase = AuthPhase.PASSWORD;
                frame.setPanel(new PassPanel());
                break;
            case PASSWORD:
                authPhase = AuthPhase.TOTP;
                frame.remove(frame.getComponent(0));
                frame.add(new TOTPPanel());
                break;
            case TOTP:
                authPhase = AuthPhase.AUTHORIZED;
                frame.remove(frame.getComponent(0));
                frame.add(new JLabel("Autenticado"));
                break;
            default:
                break;
        }
        System.out.println(authPhase);
    }
}
