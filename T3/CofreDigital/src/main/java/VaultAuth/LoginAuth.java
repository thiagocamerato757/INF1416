package VaultAuth;

import db.dao.UserDAO;
import logger.Logger;
import model.UserModel;

import java.time.LocalDateTime;

/**
 * LoginAuth class is meant to check if the email is of a valid user
 */
public class LoginAuth {
    private static final LoginAuth instance = new LoginAuth();
    private boolean validated = false;
    private String feedbackMessage = "";
    private UserModel user;

    private LoginAuth() { }

    public static LoginAuth getInstance() {
        return instance;
    }

    public boolean isValidated() {
        return validated;
    }

    public void validateLogin(String email) {
        validated = false;
        feedbackMessage = "";
        UserModel foundUser = UserDAO.getUserByLogin(email);
        if (foundUser == null) {
            Logger.log(2005);
            feedbackMessage = "Invalid email";
            return;
        }
        if (foundUser.getBloqueadoAte() != null && foundUser.getBloqueadoAte().toLocalDateTime().isAfter(LocalDateTime.now())) {
            Logger.log(2004, foundUser.getUid());
            feedbackMessage = "Usuário bloqueado até " + foundUser.getBloqueadoAte().toString();
            return;
        }
        setUser(foundUser);
        Logger.log(2003, foundUser.getUid());
        Logger.log(2002, foundUser.getUid());
        validated = true;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public UserModel getUser() {
        return user;
    }

    public void ResetAuth() {
        validated = false;
        feedbackMessage = "";
        user = null;
    }

    private void setUser(UserModel authenticatingUser) {
        user = authenticatingUser;
    }
}
