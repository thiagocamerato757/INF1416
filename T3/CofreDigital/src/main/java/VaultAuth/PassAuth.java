package VaultAuth;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import crypto.PasswordUtil;
import db.dao.UserDAO;
import model.UserModel;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * PassAuth is a class dedicated to check and validate the input password compared to the hash (+ salt) stored
 */
public class PassAuth {
    /**
     *  Instance of PassAuth
     */
    private static final PassAuth instance = new PassAuth();

    private static boolean validated = false;

    private String feedbackMessage = "";

    /**
     * Constructor
     */
    private PassAuth() {
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
    public List<String> prepPasswords(List<Entry<Integer, Integer>> inputSequence) {
        if (!passwordSizeCheck(inputSequence.size())) return null;
        return genCombinations(inputSequence);
    };

    /**
     * Checks if the calculated password hash is equal to the stored hash
     */
    public void validatePassword(List<String> possiblePasswords) {
        AuthController auth = AuthController.getInstance();
        Optional<UserModel> user = auth.getUser();
        if (!user.isPresent()) {
            feedbackMessage = "ERROR: User not found";
            return;
        }

        String hash = user.get().getSenhaBcrypt();

        if (possiblePasswords != null) {
            validated = possiblePasswords.parallelStream().anyMatch(t -> PasswordUtil.checkPassword(hash, t));
            if (validated) return;
        }
        feedbackMessage = "Password incorrect";
        updatePassError();
    }

    public void ResetAuth() {
        validated = false;
    }

    /**
     * Checks if the input password is within the 8 to 10 size rule
     * @return if the input password is from 8 to 10 size
     */
    private boolean passwordSizeCheck(int passLen) {
        return passLen >= 8 && passLen <= 10;
    }

    private List<String> genCombinations(List<Entry<Integer, Integer>> passwords) {
        List<String> passes = new ArrayList<>();
        List<String> temp = new ArrayList<>();
        passes.add("");

        for (Entry<Integer, Integer> password : passwords) {
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

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    /**
     * Adds an Error count
     */
    private void updatePassError() {
        Optional<UserModel> user = AuthController.getInstance().getUser();
        user.ifPresent(u -> {
            int err = u.getErroSenha();
            err++;
            if (err % 3 == 0) {
                u.setBloqueadoAte(Timestamp.valueOf(LocalDateTime.now().plusMinutes(2)));
                AuthController auth = AuthController.getInstance();
                auth.resetAuth();
            }
            u.setErroSenha(err);
            UserDAO.updateUser(u);
        });
    }
}
