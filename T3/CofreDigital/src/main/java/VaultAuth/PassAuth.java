package VaultAuth;

/**
 * PassAuth is a class dedicated to check and validate the input password compared to the hash (+ salt) stored
 */
public class PassAuth {
    // UserEntry = reference to user entry from sql
    /**
     * Constructor
     */
    public PassAuth() {
        // get sql instance
    }

    /**
     * Transform the input password into a hash(+salt)-ed version, appropriate for comparing
     * @param password Input password in String form
     * @return array of bytes of the hashed password
     */
    private byte[] prepPassword(String password) {
        return null;
    };

    /**
     * Checks if the calculated password hash is equal to the stored hash
     * @return if the hashes are equal
     */
    public boolean validatePassword() {
        return false;
    }

    /**
     * Checks if the input password is within the 8 to 10 size rule
     * @return if the input password is from 8 to 10 size
     */
    private boolean passwordSizeCheck(String password) {
        return password.length() >= 8 && password.length() <= 10;
    }

    /**
     * Adds an Error count
     */
    private void updatePassError() {

    }
}
