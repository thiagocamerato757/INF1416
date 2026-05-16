package crypto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Random;

/**
 * Utility for validating Digital Certificates and Private Keys.
 */
public class KeyValidator {

    /**
     * Loads an X.509 Certificate from a PEM file path.
     *
     * @param path The filesystem path to the PEM certificate file.
     * @return The loaded X509Certificate object.
     * @throws Exception If the file cannot be read or is not a valid certificate.
     */
    public static X509Certificate loadCertificate(String path) throws Exception {
        try (InputStream is = Files.newInputStream(Paths.get(path))) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(is);
        }
    }

    /**
     * Decrypts and loads a Private Key using a secret phrase.
     *
     * @param path The filesystem path to the encrypted private key file.
     * @param secretPhrase The phrase used to derive the decryption key.
     * @param cert The certificate containing the public key to determine the algorithm.
     * @return The decrypted PrivateKey object.
     * @throws Exception If decryption fails or the key format is invalid.
     */
    public static PrivateKey loadPrivateKey(String path, String secretPhrase, X509Certificate cert) throws Exception {
        byte[] encryptedKey = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));

        // Decrypt using PasswordUtil
        byte[] decryptedKey = PasswordUtil.decrypt(encryptedKey, secretPhrase);

        String algorithm = cert.getPublicKey().getAlgorithm();
        KeyFactory kf = KeyFactory.getInstance(algorithm);

        try {
            // Attempt 1: Raw PKCS8 bytes
            return kf.generatePrivate(new PKCS8EncodedKeySpec(decryptedKey));
        } catch (java.security.spec.InvalidKeySpecException e) {
            // Attempt 2: Maybe it's Base64 encoded or PEM format
            try {
                String potentialBase64 = new String(decryptedKey).trim();
                // Clean PEM headers if present
                potentialBase64 = potentialBase64.replace("-----BEGIN PRIVATE KEY-----", "")
                                               .replace("-----END PRIVATE KEY-----", "")
                                               .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                                               .replace("-----END RSA PRIVATE KEY-----", "")
                                               .replaceAll("\\s", "");
                byte[] decoded = java.util.Base64.getDecoder().decode(potentialBase64);
                return kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
            } catch (Exception e2) {
                // If both fail, throw the original exception or a clearer one
                throw new Exception("Unable to decode private key (not a valid PKCS8 format).", e);
            }
        }
    }

    /**
     * Validates if the Private Key matches the Public Key in the Certificate by signing and verifying random data.
     *
     * @param cert The certificate containing the public key.
     * @param privKey The private key to be verified.
     * @return True if the signature is valid, false otherwise.
     * @throws Exception If an error occurs during the signing or verification process.
     */
    public static boolean validateKeyPair(X509Certificate cert, PrivateKey privKey) throws Exception {
        byte[] data = new byte[9216];
        new Random().nextBytes(data);

        // Sign
        Signature sig = Signature.getInstance(cert.getSigAlgName());
        sig.initSign(privKey);
        sig.update(data);
        byte[] signature = sig.sign();

        // Verify
        sig.initVerify(cert.getPublicKey());
        sig.update(data);
        return !sig.verify(signature);
    }
}
