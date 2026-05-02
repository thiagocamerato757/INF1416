package VaultAuth;

/**
 * LoginAuth class is meant to check if the email is of a valid user
 */
public class LoginAuth {
    private static final LoginAuth instance = new LoginAuth();
    private boolean validated = false;

    /**
     * Constructor
     */
    private LoginAuth() {
        // get sql instance
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
        // checks if timed out
        // compare -> return true or false
        validated = true;
    }
}
