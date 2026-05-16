package VaultAuth;

import Cryptography.Base32;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class TOTPKeyManager {
    private static final String AES_ALGO  = "AES/ECB/PKCS5Padding";
    private static final String PRNG_ALGO = "SHA1PRNG";
    private static final int    AES_BITS  = 32; // 256 bits = 32 bytes
    private static final int    SECRET_BYTES = 20;

    // -------------------------------------------------------------- //
    //  Geração da chave secreta (cadastro do usuário)                 //
    // -------------------------------------------------------------- //

    /**
     * Gera 20 bytes aleatórios e retorna a chave codificada em BASE32.
     * Deve ser chamado UMA vez no momento do cadastro.
     */
    public static String generateBase32Secret() throws Exception {
        SecureRandom rng = new SecureRandom();
        byte[] secret = new byte[SECRET_BYTES];
        rng.nextBytes(secret);
        Base32 b = new Base32(Base32.Alphabet.BASE32, false, false);
        return b.toString(secret);
    }

    // -------------------------------------------------------------- //
    //  Derivação da chave AES a partir da senha (SHA1PRNG)            //
    // -------------------------------------------------------------- //

    /**
     * Deriva uma chave AES-256 determinística a partir da senha do usuário
     * usando SHA1PRNG como gerador (comportamento reprodutível com mesma seed).
     */
    private static SecretKeySpec deriveAESKey(String password) throws Exception {
        SecureRandom sr = SecureRandom.getInstance(PRNG_ALGO);
        sr.setSeed(password.getBytes(StandardCharsets.UTF_8));
        byte[] keyBytes = new byte[AES_BITS];
        sr.nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, "AES");
    }

    // -------------------------------------------------------------- //
    //  Criptografia / Decriptografia para armazenamento no banco      //
    // -------------------------------------------------------------- //

    /**
     * Criptografa a chave BASE32 com AES/ECB/PKCS5Padding.
     * O resultado (byte[]) é o que se persiste na tabela Usuarios.
     */
    public static byte[] encryptSecret(String base32Secret, String password)
            throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, deriveAESKey(password));
        return cipher.doFinal(base32Secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decriptografa o blob armazenado no banco e retorna a chave BASE32.
     * Chamar antes de instanciar TOTP.
     */
    public static String decryptSecret(byte[] encryptedSecret, String password)
            throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, deriveAESKey(password));
        return new String(cipher.doFinal(encryptedSecret), StandardCharsets.UTF_8);
    }
}