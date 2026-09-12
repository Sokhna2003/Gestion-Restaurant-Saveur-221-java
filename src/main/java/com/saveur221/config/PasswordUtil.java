package com.saveur221.config;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Gère le hachage et la vérification des mots de passe.
 * <p>
 * La base "restaurant_saveur221" est partagée avec le Module B (PHP) : PHP
 * normalise tous les mots de passe en bcrypt ($2y$/$2b$/$2a$). Ce module doit
 * pouvoir vérifier ces hashs (bcrypt) tout en restant compatible avec les
 * hashs SHA-256 insérés par le script SQL du module Java.
 * </p>
 */
public class PasswordUtil {

    private static final int COUT_BCRYPT = 10;

    private PasswordUtil() {
    }

    /**
     * Produit un hash bcrypt, format identique à celui que PHP utilise
     * (password_hash/PASSWORD_DEFAULT), afin qu'un compte créé côté Java
     * soit lisible aussi par le Module B.
     */
    public static String hash(String motDePasseClair) {
        return BCrypt.hashpw(motDePasseClair, BCrypt.gensalt(COUT_BCRYPT));
    }

    /**
     * Vérifie un mot de passe contre le hash stocké.
     * <ul>
     *   <li>Hash bcrypt ($2y$, $2b$, $2a$) : vérification bcrypt ;</li>
     *   <li>Sinon : SHA-256 (calculé à la volée, pour les hashs hex de 64 car.
     *       insérés par le script SQL Java).</li>
     * </ul>
     */
    public static boolean verifier(String motDePasseClair, String hashStocke) {
        if (motDePasseClair == null || hashStocke == null) {
            return false;
        }
        if (estBcrypt(hashStocke)) {
            return verifierBcrypt(motDePasseClair, hashStocke);
        }
        return hashSha256(motDePasseClair).equals(hashStocke);
    }

    private static boolean estBcrypt(String hashStocke) {
        return hashStocke.startsWith("$2y$")
                || hashStocke.startsWith("$2b$")
                || hashStocke.startsWith("$2a$");
    }

    private static boolean verifierBcrypt(String motDePasseClair, String hashStocke) {
        // jbcrypt 0.4 ne gère que le préfixe "$2a$" : "$2y$"/"$2b$" sont
        // interchangeables avec "$2a$" (mêmes sel + hash), on normalise avant comparaison.
        String hashNormalise = hashStocke.startsWith("$2a$")
                ? hashStocke
                : "$2a$" + hashStocke.substring(4);
        return BCrypt.checkpw(motDePasseClair, hashNormalise);
    }

    /**
     * SHA-256 en hexadécimal, utilisé uniquement pour les hashs legacy
     * insérés par le script SQL du module Java.
     */
    private static String hashSha256(String motDePasseClair) {
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
}