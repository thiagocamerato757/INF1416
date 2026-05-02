package VaultAuth;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PassAuth is a class dedicated to check and validate the input password compared to the hash (+ salt) stored
 */
public class PassAuth {
    // UserEntry = reference to user entry from sql

    /**
     *  Instance of PassAuth
     */
    private static final PassAuth instance = new PassAuth();

    private static boolean validated = false;

    /**
     * Constructor
     */
    private PassAuth() {
        // get sql instance
    }

    /**
     * Gets instantiation of PassAuth
     * @return current instance
     */
    public static PassAuth getInstance() {
        return instance;
    }

    public boolean isValidated() {
        return validated;
    }

    /**
     * Transform the input password into a hash(+salt)-ed version, appropriate for comparing
     * @param inputSequence Sequence of inputs from the virtual keyboard
     * @return list of possible passwords
     */
    public List<String> prepPasswords(List<Pair<Integer, Integer>> inputSequence) {
        if (!passwordSizeCheck(inputSequence.size())) return null;
        return genCombinations(inputSequence);
    };

    /**
     * Checks if the calculated password hash is equal to the stored hash
     */
    public void validatePassword(List<String> possiblePasswords) {
        String hash = ""; // get from Sql
        if (possiblePasswords != null)
            //validated = possiblePasswords.parallelStream().anyMatch(t -> BCrypt.checkpw(t, hash));
            validated = true;
    }

    /**
     * Checks if the input password is within the 8 to 10 size rule
     * @return if the input password is from 8 to 10 size
     */
    private boolean passwordSizeCheck(int passLen) {
        return passLen >= 8 && passLen <= 10;
    }

    private List<String> genCombinations(List<Pair<Integer, Integer>> passwords) {
        List<String> passes = new ArrayList<>();
        List<String> temp = new ArrayList<>();
        passes.add("");

        for (Pair<Integer, Integer> password : passwords) {
            Integer A = password.getKey();
            Integer B = password.getValue();
            for (String pass : passes) {
                temp.add(pass + A);
                temp.add(pass + B);
            }
            passes.clear();
            passes.addAll(temp);
            temp.clear();
        }

        return passes;
    }

    /**
     * Adds an Error count
     */
    private void updatePassError() {

    }
}
