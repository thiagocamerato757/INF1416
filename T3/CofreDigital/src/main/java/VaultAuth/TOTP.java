package VaultAuth;

import Cryptography.Base32;
import db.dao.UserDAO;
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
 * TOTP class is dedicated for authenticating the user's possession of the TOTP
 */
public class TOTP {
    private static TOTP instance = new TOTP();
    private boolean validated = false;
    private String feedbackMessage = "";

    private byte[] key = null;
    private long timeStepInSeconds = 30;

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
        validated = false;
    }

    public void setPass(String password) {
        this.password = password;
    }

    private void getKey() {
        AuthController ctrl = AuthController.getInstance();
        Optional<UserModel> user = ctrl.getUser();

        user.ifPresent(u -> {
           byte[] enc = u.getTotpSecretEncrypted();

           try {
               String base32Secret = TOTPKeyManager.decryptSecret(enc, password);
               key = new Base32(Base32.Alphabet.BASE32, false, false).fromString(base32Secret);
           } catch (Exception e) {
               System.out.println("Error decrypting key: " + e.getMessage());
           }
        });
    }

    /**
     * Recebe o HASH HMAC-SHA1 e determina o código TOTP de 6 algarismos
     * decimais, prefixado com zeros quando necessário.
     * @param hash Hashed secret
     * @return TOTP code
     */
    private String getTOTPCodeFromHash(byte[] hash) {
        // Dynamic truncation: usa os 4 bits menos significativos do último byte como offset
        int offset = hash[hash.length - 1] & 0x0F;

        // Extrai 4 bytes a partir do offset, mascarando o bit de sinal do primeiro
        int binary = ((hash[offset]     & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) <<  8)
                |  (hash[offset + 3] & 0xFF);

        // Reduz para 6 dígitos e formata com zero-padding
        int otp = binary % 1_000_000;
        return String.format("%06d", otp);
    }

    /**
     * Recebe o contador e a chave secreta para produzir o hash HMAC-SHA1.
     * @param counter counter
     * @param keyByteArray Byte array of the key
     * @return HMAC-SHA1 hash
     */
    private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(keyByteArray, "HmacSHA1"));
            return mac.doFinal(counter);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Erro ao calcular HMAC-SHA1", e);
        }
    }

    /**
     * Recebe o intervalo de tempo e executa o algoritmo TOTP para produzir
     * o código TOTP. Usa os métodos auxiliares getTOTPCodeFromHash e HMAC_SHA1.
     * @param timeInterval time interval
     * @return TOTP code
     */
    private String TOTPCode(long timeInterval) {
        // Converte o intervalo de tempo em array de 8 bytes (big-endian)
        byte[] counter = ByteBuffer.allocate(8).putLong(timeInterval).array();
        byte[] hash = HMAC_SHA1(counter, key);
        return getTOTPCodeFromHash(hash);
    }

    /**
     * Metodo que é utilizado para validar um código TOTP (inputTOTP).
     * Deve considerar um atraso ou adiantamento de 30 segundos no
     * relógio da máquina que gerou o código TOTP.
     * @param inputTOTP Input TOTP code
     */
    public void validateCode(String inputTOTP) {
        if (key == null) {
            getKey();
            if (key == null) {
                validated = false;
                feedbackMessage = "An error has occurred while validating the TOTP code";
                return;
            }
        }
        long timeInterval = System.currentTimeMillis() / 1000L / 30L;

        // Aceita o intervalo atual e os vizinhos imediatos (±30 s de tolerância)
        for (long delta = -1; delta <= 1; delta++) {
            if (TOTPCode(timeInterval + delta).equals(inputTOTP)) {
                validated = true;
                feedbackMessage = "";
                return;
            }
        }

        validated = false;
        feedbackMessage = "Incorrect TOTP code";
        updateErrorCount();
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    private void updateErrorCount() {
        Optional<UserModel> user = AuthController.getInstance().getUser();
        user.ifPresent(u -> {
            int err = u.getErroToken();
            err++;
            if (err >= 3) {
                u.setBloqueadoAte(Timestamp.valueOf(LocalDateTime.now().plusMinutes(2)));
                AuthController auth = AuthController.getInstance();
                auth.resetAuth();
                err = 0;
            }
            u.setErroToken(err);
            UserDAO.updateUser(u);
        });
    }
}