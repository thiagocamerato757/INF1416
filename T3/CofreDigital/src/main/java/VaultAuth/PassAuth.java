package VaultAuth;

import crypto.PasswordUtil;
import db.dao.UserDAO;
import logger.Logger;
import model.UserModel;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * PassAuth checks the password candidates produced by the overloaded virtual keyboard.
 */
public class PassAuth {
    private static final PassAuth instance = new PassAuth();
    private static boolean validated = false;
    private String feedbackMessage = "";

    private PassAuth() { }

    public static PassAuth getInstance() {
        return instance;
    }

    public boolean isValidated() {
        return validated;
    }

    public List<String> prepPasswords(List<Entry<Integer, Integer>> inputSequence) {
        if (!passwordSizeCheck(inputSequence.size())) return null;
        return genCombinations(inputSequence);
    }

    public void validatePassword(List<String> possiblePasswords) {
        validated = false;
        feedbackMessage = "";
        Optional<UserModel> user = AuthController.getInstance().getUser();
        if (!user.isPresent()) {
            feedbackMessage = "ERROR: User not found";
            return;
        }

        String hash = user.get().getSenhaBcrypt();
        if (possiblePasswords != null) {
            Optional<String> found = possiblePasswords.parallelStream()
                    .filter(t -> PasswordUtil.checkPassword(hash, t))
                    .findAny();
            String password = found.orElse(null);
            if (password != null) {
                UserModel u = user.get();
                u.setErroSenha(0);
                UserDAO.updateUser(u);
                TOTP.getInstance().setPass(password);
                Logger.log(3003, u.getUid(), u.getLogin());
                Logger.log(3002, u.getUid(), u.getLogin());
                validated = true;
                return;
            }
        }
        feedbackMessage = "Password incorrect";
        updatePassError();
    }

    public void ResetAuth() {
        validated = false;
        feedbackMessage = "";
    }

    private boolean passwordSizeCheck(int passLen) {
        return passLen >= 8 && passLen <= 10;
    }

    private List<String> genCombinations(List<Entry<Integer, Integer>> passwords) {
        List<String> passes = new ArrayList<>();
        List<String> temp = new ArrayList<>();
        passes.add("");

        for (Entry<Integer, Integer> password : passwords) {
            Integer a = password.getKey();
            Integer b = password.getValue();
            for (String pass : passes) {
                temp.add(pass + a);
                temp.add(pass + b);
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

    private void updatePassError() {
        Optional<UserModel> user = AuthController.getInstance().getUser();
        user.ifPresent(u -> {
            int err = u.getErroSenha() + 1;
            if (err == 1) Logger.log(3004, u.getUid(), u.getLogin());
            if (err == 2) Logger.log(3005, u.getUid(), u.getLogin());
            if (err >= 3) {
                Logger.log(3006, u.getUid(), u.getLogin());
                Logger.log(3007, u.getUid(), u.getLogin());
                u.setBloqueadoAte(Timestamp.valueOf(LocalDateTime.now().plusMinutes(2)));
                err = 0;
            }
            u.setErroSenha(err);
            UserDAO.updateUser(u);
            if (u.getBloqueadoAte() != null && u.getBloqueadoAte().toLocalDateTime().isAfter(LocalDateTime.now())) {
                AuthController.getInstance().restartAuthentication();
            }
        });
    }
}
