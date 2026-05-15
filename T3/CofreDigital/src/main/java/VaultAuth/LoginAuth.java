package VaultAuth;

import db.dao.UserDAO;
import model.UserModel;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * LoginAuth class is meant to check if the email is of a valid user
 */
public class LoginAuth {
    private static final LoginAuth instance = new LoginAuth();
    private boolean validated = false;
    private String feedbackMessage = "";
    private UserModel user;

    /**
     * Constructor
     */
    private LoginAuth() {
    }

    public static LoginAuth getInstance() {
        return instance;
    }

    public boolean isValidated() {
        return validated;
    }

    /**
     * Validates the existence of a user with the same input email
     * @param email Input email
     */
    public void validateLogin(String email) {
        // sql call to find email
        UserModel user = UserDAO.getUserByLogin(email);
        // checks if exist
        if (user == null) {
            feedbackMessage = "Invalid email";
            return;
        }
        // checks if timed out
        if (user.getBloqueadoAte() != null && user.getBloqueadoAte().toLocalDateTime().isAfter(LocalDateTime.now())) {
            feedbackMessage = "Usuário bloqueado até " + user.getBloqueadoAte().toString();
            return;
        }
        // else
        setUser(user);
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
    }

    private void setUser(UserModel authenticatingUser) {
        user = authenticatingUser;
    }
}
