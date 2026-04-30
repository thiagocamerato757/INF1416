package VaultAuth;

/**
 * LoginAuth class is meant to check if the email is of a valid user
 */
public class LoginAuth {
    /**
     * Constructor
     */
    public LoginAuth() {
        // get sql instance
    }

    /**
     * Validates the existence of a user with the same input email
     * @param email Input email
     * @return if it is a registered email
     */
    public boolean validateLogin(String email) {
        // sql call to find email
        // compare -> return true or false
        return false;
    }
}
