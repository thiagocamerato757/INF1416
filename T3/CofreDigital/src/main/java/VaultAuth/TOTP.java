package VaultAuth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

/**
 * TOTP class is dedicated for authenticating the user's possession of the TOTP
 */
public class TOTP {
    private static TOTP instance = new TOTP();
    private boolean validated = false;

    private byte[] key = null;
    private long timeStepInSeconds = 30;

    /**
     * Construtor da classe. Recebe a chave secreta em BASE32 e o intervalo
     * de tempo a ser adotado (default = 30 segundos). Deve decodificar a
     * chave secreta e armazenar em key. Em caso de erro, gera Exception.
     * @param base32EncodedSecret Secret from database encoded in base32
     * @param timeStepInSeconds Time interval in between OTPs
     * @throws Exception TBD
     */
    private TOTP(String base32EncodedSecret, long timeStepInSeconds)
            throws Exception {

    }

    private TOTP() {

    }

    public static TOTP getInstance() {
        return instance;
    }

    public boolean isValidated() {
        return validated;
    }

    public void ResetAuth() {
        validated = false;
    }

    /**
     * Recebe o HASH HMAC-SHA1 e determina o código TOTP de 6 algarismos
     * decimais, prefixado com zeros quando necessário.
     * @param hash Hashed secret
     * @return TOTP code
     */
    private String getTOTPCodeFromHash(byte[] hash) {
        return null;
    }

    /**
     * Recebe o contador e a chave secreta para produzir o hash HMAC-SHA1.
     * @param counter counter
     * @param keyByteArray Byte array of the key
     * @return HMAC-SHA1 hash
     */
    private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) {
        return null;
    }

    /**
     * Recebe o intervalo de tempo e executa o algoritmo TOTP para produzir
     * o código TOTP. Usa os métodos auxiliares getTOTPCodeFromHash e HMAC_SHA1.
     * @param timeInterval time interval
     * @return TOTP code
     */
    private String TOTPCode(long timeInterval) {
        return null;
    }

    /**
     * Metodo que é utilizado para solicitar a geração do código TOTP.
     * @return TOTP code
     */
    public String generateCode() {
        return null;
    }
    //

    /**
     * Metodo que é utilizado para validar um código TOTP (inputTOTP).
     * Deve considerar um atraso ou adiantamento de 30 segundos no
     * relógio da máquina que gerou o código TOTP.
     * @param inputTOTP Input TOTP code
     */
    public void validateCode(String inputTOTP) {


        validated = true;
    }
}