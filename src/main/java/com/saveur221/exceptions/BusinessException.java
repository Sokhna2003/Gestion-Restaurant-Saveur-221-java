package com.saveur221.exceptions;

/**
 * Exception racine pour toute violation d'une règle métier du restaurant
 * (stock insuffisant, catégorie utilisée, paiement excessif, etc.).
 * Les repositories, eux, lèveront plutôt des SQLException techniques.
 */
public class BusinessException extends Exception {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
