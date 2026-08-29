package src.main.java.com.saveur221.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hache les mots de passe en SHA-256 avant stockage/comparaison.
 * On ne garde jamais un mot de passe en clair en base.
 */
public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String motDePasseClair) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(motDePasseClair.getBytes("UTF-8"));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }

    public static boolean verifier(String motDePasseClair, String hashStocke) {
        return hash(motDePasseClair).equals(hashStocke);
    }
}
