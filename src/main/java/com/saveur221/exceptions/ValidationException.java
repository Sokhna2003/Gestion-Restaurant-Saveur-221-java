package com.saveur221.exceptions;

/**
 * Levée quand une donnée fournie ne respecte pas une contrainte métier
 * (ex: mot de passe < 6 caractères, email déjà utilisé, catégorie
 * contenant des produits qu'on tente de supprimer, paiement > montant restant...).
 */
public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super(message);
    }
}
