package crypto;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Utility class for password hashing and AES encryption/decryption.
 * Implements:
 * - Bcrypt hashing (2y) for personal passwords
 * - AES-256 key derivation using SHA1PRNG
 * - AES/ECB/PKCS5Padding encryption and decryption
 * - Personal password format validation
 */
public class PasswordUtil {

    private static final int BCRYPT_COST = 8;

    /**
     * Generates a bcrypt 2y hash for the personal password.
     * Returns a 60-character string in the format: $2y$08$+Salt+Hash
     *
     * @param plainPassword password in plain text (e.g. "13572468")
     * @return bcrypt hash ready to be stored in the database
     */
    public static String hashPassword(String plainPassword) {
        // Generates a random 16-byte salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return OpenBSDBCrypt.generate("2y", plainPassword.toCharArray(), salt, BCRYPT_COST);
    }

    /**
     * Verifies whether the plain text password matches the stored bcrypt hash.
     *
     * @param storedHash hash stored in the database ($2y$08$...)
     * @param plainPassword password provided by the user
     * @return true if the password is valid
     */
    public static boolean checkPassword(String storedHash, String plainPassword) {
        return OpenBSDBCrypt.checkPassword(storedHash, plainPassword.toCharArray());
    }


    /**
     * Derives an AES-256 key from a password (secret phrase or personal password).
     * Uses KeyGenerator + SecureRandom with SHA1PRNG.
     *
     * @param password password used as seed
     * @return 256-bit AES SecretKey
     */
    public static SecretKey deriveAESKey(String password) throws Exception {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(password.getBytes(StandardCharsets.UTF_8));

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, secureRandom);

        return keyGen.generateKey();
    }

    // -------------------------------------------------------------------------
    // AES ECB/PKCS5
    // -------------------------------------------------------------------------

    /**
     * Encrypts data using AES/ECB/PKCS5Padding with a key derived from the password.
     * Used to encrypt: TOTP secret (base32) and user's private key.
     *
     * @param data plain text data (bytes)
     * @param password password used to derive the AES key
     * @return encrypted data
     */
    public static byte[] encrypt(byte[] data, String password) throws Exception {
        SecretKey key = deriveAESKey(password);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * Decrypts data using AES/ECB/PKCS5Padding with a key derived from the password.
     *
     * @param encryptedData encrypted data
     * @param password password used to derive the AES key
     * @return decrypted data
     */
    public static byte[] decrypt(byte[] encryptedData, String password) throws Exception {
        SecretKey key = deriveAESKey(password);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }


    /**
     * Validates the personal password according to the required rules:
     * - 8 to 10 digits
     * - numeric characters only (0-9)
     * - cannot contain all identical digits (e.g. "11111111")
     *
     * @param password password to validate
     * @return true if valid
     */
    public static boolean isValidPersonalPassword(String password) {
        if (password == null) return false;

        // Must contain 8 to 10 numeric digits
        if (!password.matches("\\d{8,10}")) return false;

        // Cannot contain only repeated digits
        // Examples: "11111111", "2222222222" are invalid
        char first = password.charAt(0);
        boolean allSame = true;

        for (char c : password.toCharArray()) {
            if (c != first) {
                allSame = false;
                break;
            }
        }

        return !allSame;
    }
}