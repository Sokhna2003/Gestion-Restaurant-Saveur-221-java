package com.saveur221.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Vérifie que PasswordUtil accepte les trois formats présents dans la base
 * partagée : bcrypt (PHP), SHA-256 (seed du script Java) et clair (seed PHP).
 */
class PasswordUtilTest {

    /** Hash réellement stocké en base pour admin@saveur221.sn (bcrypt généré par PHP). */
    private static final String BCRYPT_ADMIN_BDD =
            "$2y$10$2IIESCMTsLfaBRgz.XE3i.1fsfjC.2iPMjGyZX97bRRovx33gH8pG";

    private static final String SHA256_ADMIN123 =
            "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";

    @Test
    void verifieUnHashBcryptStockeParPhp() {
        assertTrue(PasswordUtil.verifier("admin123", BCRYPT_ADMIN_BDD));
        assertFalse(PasswordUtil.verifier("mauvais", BCRYPT_ADMIN_BDD));
    }

    @Test
    void verifieUnHashBcryptEnPrefx2b() {
        String en2a = PasswordUtil.hash("saveur221");
        String en2b = "$2b$" + en2a.substring(4);
        assertTrue(PasswordUtil.verifier("saveur221", en2b));
    }

    @Test
    void verifieUnHashSha256DuScriptSql() {
        assertTrue(PasswordUtil.verifier("admin123", SHA256_ADMIN123));
        assertFalse(PasswordUtil.verifier("mauvais", SHA256_ADMIN123));
    }

    @Test
    void unHashProduitSeVerifie() {
        String hash = PasswordUtil.hash("thiebou-dieun");
        assertTrue(PasswordUtil.verifier("thiebou-dieun", hash));
        assertFalse(PasswordUtil.verifier("autre-mot-de-passe", hash));
    }

    @Test
    void onNeCrashePasSurDesValeursNullOuInconnues() {
        assertFalse(PasswordUtil.verifier(null, "xx"));
        assertFalse(PasswordUtil.verifier("admin123", null));
        assertFalse(PasswordUtil.verifier("admin123", "format-inconnu"));
    }
}