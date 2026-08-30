package com.saveur221.exceptions;

/**
 * Levée en cas d'échec de connexion : email inconnu, mot de passe incorrect,
 * ou compte désactivé (règle métier n°1 du sujet).
 */
public class AuthentificationException extends BusinessException {
    public AuthentificationException(String message) {
        super(message);
    }
}
