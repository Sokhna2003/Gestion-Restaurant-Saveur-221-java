package com.saveur221.exceptions;

/**
 * Levée quand on tente de commander/retirer plus que la quantité disponible.
 * Règle métier n°6 du sujet.
 */
public class StockInsuffisantException extends BusinessException {
    public StockInsuffisantException(String produit, int demande, int disponible) {
        super("Stock insuffisant pour \"" + produit + "\" : demandé " + demande
                + ", disponible " + disponible);
    }
}
