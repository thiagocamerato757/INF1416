package VaultAuth;

import crypto.PasswordUtil;
import logger.Logger;
import db.dao.KeyDAO;
import db.dao.UserDAO;
import model.UserModel;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;

public class AdminController {

    private static String adminSecretPhrase = null;
    private static String lastGeneratedTotpSecret = null;
    private static final String EMAIL_OID = "1.2.840.113549.1.9.1";

    public static String getAdminSecretPhrase() {
        return adminSecretPhrase;
    }

    public static String getLastGeneratedTotpSecret() {
        return lastGeneratedTotpSecret;
    }

    public static void clearAdminSecretPhrase() {
        adminSecretPhrase = null;
        lastGeneratedTotpSecret = null;
    }

    /**
     * Performs the first-time admin setup.setup.
     * @return null on success, or an error message on failure
     */
    public static String setupAdmin(String certPath, String keyPath, String secretPhrase, String personalPassword) {
        X509Certificate cert;
        try {
            cert = readCertificate(certPath);
        } catch (Exception e) {
            return "Invalid certificate file: " + e.getMessage();
        }

        PrivateKey privateKey;
        try {
            byte[] encryptedKeyBytes = Files.readAllBytes(Paths.get(keyPath));
            byte[] decryptedBytes = PasswordUtil.decrypt(encryptedKeyBytes, secretPhrase);
            String base64Key = new String(decryptedBytes, StandardCharsets.UTF_8).trim();
            if (base64Key.contains("-----BEGIN")) {
                StringBuilder base64 = new StringBuilder();
                for (String line : base64Key.split("\n")) {
                    line = line.trim();
                    if (!line.startsWith("-----")) {
                        base64.append(line);
                    }
                }
                base64Key = base64.toString();
            } else {
                base64Key = base64Key.replaceAll("\\s+", "");
            }
            byte[] pkcs8Bytes = Base64.getDecoder().decode(base64Key);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8Bytes));
        } catch (Exception e) {
            return "Invalid private key or wrong secret phrase: " + e.getMessage();
        }

        if (validateKeyPair(privateKey, cert.getPublicKey())) {
            return "Private key does not match certificate";
        }

        String name = extractCN(cert);
        String login = extractEmail(cert);
        if (login == null) {
            return "Certificate does not contain an email address in the Subject field";
        }
        if (name == null) name = login;

        if (!PasswordUtil.isValidPersonalPassword(personalPassword)) {
            return "Invalid personal password: must be 8-10 digits, not all identical";
        }

        String hashedPassword = PasswordUtil.hashPassword(personalPassword);

        String totpSecret;
        byte[] encryptedTotp;
        try {
            totpSecret = TOTPKeyManager.generateBase32Secret();
            encryptedTotp = TOTPKeyManager.encryptSecret(totpSecret, personalPassword);
        } catch (Exception e) {
            return "Failed to generate TOTP secret: " + e.getMessage();
        }

        byte[] encryptedPrivateKey;
        try {
            byte[] keyBytes = privateKey.getEncoded();
            encryptedPrivateKey = PasswordUtil.encrypt(keyBytes, secretPhrase);
        } catch (Exception e) {
            return "Failed to encrypt private key: " + e.getMessage();
        }

        String certPem;
        try {
            certPem = new String(Files.readAllBytes(Paths.get(certPath)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Failed to read certificate file: " + e.getMessage();
        }

        int kid = KeyDAO.createKey(certPem, encryptedPrivateKey);
        if (kid <= 0) {
            return "Failed to store key pair in database";
        }

        UserModel admin = new UserModel();
        admin.setLogin(login);
        admin.setNome(name);
        admin.setSenhaBcrypt(hashedPassword);
        admin.setTotpSecretEncrypted(encryptedTotp);
        admin.setGrupoId(1);
        admin.setKid(kid);

        int uid = UserDAO.createUser(admin);
        if (uid <= 0) {
            return "Failed to create administrator user";
        }
        UserDAO.updateKeypairUID(kid, uid);

        adminSecretPhrase = secretPhrase;
        lastGeneratedTotpSecret = totpSecret;

        Logger.log(1005, uid);
        return null;
    }

    /**
     * Validates the admin's secret phrase on subsequent system startups.
     * @return null on success, or an error message on failure
     */
    public static String validateAdmin(String secretPhrase) {
        UserModel admin = UserDAO.getFirstUser();
        if (admin == null) {
            return "No administrator found in database";
        }

        Integer kid = admin.getKid();
        if (kid == null) {
            return "Administrator has no key pair registered";
        }

        Object[] certAndKey = KeyDAO.getCertAndKey(kid);
        if (certAndKey == null) {
            return "Failed to retrieve administrator key pair";
        }

        String certPem = (String) certAndKey[0];
        byte[] encryptedPrivateKey = (byte[]) certAndKey[1];

        PrivateKey privateKey;
        try {
            byte[] pkcs8Bytes = PasswordUtil.decrypt(encryptedPrivateKey, secretPhrase);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8Bytes));
        } catch (Exception e) {
            return "Invalid secret phrase: could not decrypt private key";
        }

        X509Certificate cert;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(
                    new java.io.ByteArrayInputStream(certPem.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return "Failed to parse stored certificate";
        }

        if (validateKeyPair(privateKey, cert.getPublicKey())) {
            return "Private key does not match certificate";
        }

        adminSecretPhrase = secretPhrase;
        Logger.log(1006, admin.getUid(), "Partida do sistema iniciada para operaÃƒÂ§ÃƒÂ£o normal pelos usuÃƒÂ¡rios.");
        return null;
    }

    private static X509Certificate readCertificate(String path) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (InputStream in = Files.newInputStream(Paths.get(path))) {
            return (X509Certificate) cf.generateCertificate(in);
        }
    }

    private static boolean validateKeyPair(PrivateKey privateKey, PublicKey publicKey) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] data = new byte[9216];
            random.nextBytes(data);

            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(data);
            byte[] signature = signer.sign();

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(data);
            return !verifier.verify(signature);
        } catch (Exception e) {
            return true;
        }
    }

    private static String extractCN(X509Certificate cert) {
        String dn = cert.getSubjectX500Principal().getName();
        for (String part : dn.split(",")) {
            part = part.trim();
            if (part.toUpperCase().startsWith("CN=")) {
                return part.substring(3).trim();
            }
        }
        return null;
    }

    private static String extractEmail(X509Certificate cert) {
        // 1. SubjectAlternativeNames
        try {
            Collection<List<?>> sans = cert.getSubjectAlternativeNames();
            if (sans != null) {
                for (List<?> san : sans) {
                    if (san.size() >= 2 && Integer.valueOf(1).equals(san.get(0))) {
                        return rdnValueToString(san.get(1));
                    }
                }
            }
        } catch (CertificateParsingException ignored) {}

        // 2. Subject DN Ã¢â‚¬â€ fallback para certificados legados
        String dn = cert.getSubjectX500Principal().getName(X500Principal.RFC2253);
        try {
            LdapName ldapName = new LdapName(dn);
            for (Rdn rdn : ldapName.getRdns()) {
                if (isEmailType(rdn.getType())) {
                    return rdnValueToString(rdn.getValue());
                }
            }
        } catch (InvalidNameException e) {
            // Parse manual: split respeita vÃƒÂ­rgulas dentro de aspas
            for (String part : dn.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
                part = part.trim();
                int eq = part.indexOf('=');
                if (eq < 0) continue;
                if (isEmailType(part.substring(0, eq).trim())) {
                    return decodeHexValue(part.substring(eq + 1).trim());
                }
            }
        }

        return null;
    }

    private static boolean isEmailType(String type) {
        return type.equalsIgnoreCase("EMAILADDRESS")
                || type.equals(EMAIL_OID)
                || type.equalsIgnoreCase("E");
    }

    private static String rdnValueToString(Object value) {
        String text;
        if (value instanceof String) {
            text = (String) value;
        } else if (value instanceof byte[]) {
            text = decodeAsn1String((byte[]) value);
        } else {
            text = value.toString();
        }
        return UserDAO.normalizeLogin(text);
    }

    private static String decodeAsn1String(byte[] bytes) {
        if (bytes.length >= 2) {
            int tag = bytes[0] & 0xFF;
            int len = bytes[1] & 0xFF;
            if ((tag == 0x16 || tag == 0x0C || tag == 0x13 || tag == 0x14) && len <= bytes.length - 2) {
                return new String(bytes, 2, len, StandardCharsets.UTF_8);
            }
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String decodeHexValue(String value) {
        if (value.startsWith("#")) {
            String hex = value.substring(1);
            int contentLen = Integer.parseInt(hex.substring(2, 4), 16);
            byte[] bytes = new byte[contentLen];
            for (int i = 0; i < contentLen; i++) {
                bytes[i] = (byte) Integer.parseInt(hex.substring(4 + i * 2, 6 + i * 2), 16);
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return value;
    }
}
