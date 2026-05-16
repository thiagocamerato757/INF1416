package VaultAuth;

import Cryptography.Base32;
import db.dao.UserDAO;
import logger.Logger;
import model.UserModel;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * TOTP authentication using RFC 4226/6238 primitives required by the assignment.
 */
public class TOTP {
    private static final TOTP instance = new TOTP();
    private boolean validated = false;
    private String feedbackMessage = "";
    private byte[] key = null;
    private String password = null;

    private TOTP() { }

    public static TOTP getInstance() {
        return instance;
    }

    public boolean isValidated() {
        return validated;
    }

    public void ResetAuth() {
        key = null;
        password = null;
        validated = false;
        feedbackMessage = "";
    }

    public void setPass(String password) {
        this.password = password;
    }

    private void getKey() {
        Optional<UserModel> user = AuthController.getInstance().getUser();
        user.ifPresent(u -> {
            try {
                String base32Secret = TOTPKeyManager.decryptSecret(u.getTotpSecretEncrypted(), password);
                key = new Base32(Base32.Alphabet.BASE32, false, false).fromString(base32Secret);
            } catch (Exception e) {
                feedbackMessage = "Error decrypting TOTP key";
            }
        });
    }

    private String getTOTPCodeFromHash(byte[] hash) {
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);
        return String.format("%06d", binary % 1_000_000);
    }

    private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(keyByteArray, "HmacSHA1"));
            return mac.doFinal(counter);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Erro ao calcular HMAC-SHA1", e);
        }
    }

    private String TOTPCode(long timeInterval) {
        byte[] counter = ByteBuffer.allocate(8).putLong(timeInterval).array();
        return getTOTPCodeFromHash(HMAC_SHA1(counter, key));
    }

    public void validateCode(String inputTOTP) {
        validated = false;
        feedbackMessage = "";
        if (key == null) {
            getKey();
            if (key == null) {
                feedbackMessage = "An error has occurred while validating the TOTP code";
                return;
            }
        }
        long timeStepInSeconds = 30;
        long timeInterval = System.currentTimeMillis() / 1000L / timeStepInSeconds;
        for (long delta = -1; delta <= 1; delta++) {
            if (TOTPCode(timeInterval + delta).equals(inputTOTP)) {
                Optional<UserModel> user = AuthController.getInstance().getUser();
                user.ifPresent(u -> {
                    u.setErroToken(0);
                    UserDAO.updateUser(u);
                    Logger.log(4003, u.getUid());
                    Logger.log(4002, u.getUid());
                });
                validated = true;
                return;
            }
        }
        feedbackMessage = "Incorrect TOTP code";
        updateErrorCount();
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    private void updateErrorCount() {
        Optional<UserModel> user = AuthController.getInstance().getUser();
        user.ifPresent(u -> {
            int err = u.getErroToken() + 1;
            if (err == 1) Logger.log(4004, u.getUid());
            if (err == 2) Logger.log(4005, u.getUid());
            if (err >= 3) {
                Logger.log(4006, u.getUid());
                Logger.log(4007, u.getUid());
                u.setBloqueadoAte(Timestamp.valueOf(LocalDateTime.now().plusMinutes(2)));
                err = 0;
            }
            u.setErroToken(err);
            UserDAO.updateUser(u);
            if (u.getBloqueadoAte() != null && u.getBloqueadoAte().toLocalDateTime().isAfter(LocalDateTime.now())) {
                AuthController.getInstance().restartAuthentication();
            }
        });
    }
}
