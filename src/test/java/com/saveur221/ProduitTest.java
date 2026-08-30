package com.saveur221;

import com.saveur221.entities.Produit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exemple de test unitaire sur une règle métier simple portée par l'entité.
 * D'autres tests viendront sur les Services une fois écrits (Sprint 2/3).
 */
class ProduitTest {

    @Test
    void unProduitAvecStockZeroEstEnRupture() {
        Produit produit = new Produit(1, "Thieboudienne", "Riz au poisson",
                new BigDecimal("3500"), 0, 5, null, false, null);

        assertTrue(produit.isEnRupture());
        assertFalse(produit.isStockFaible());
    }

    @Test
    void unProduitSousLeSeuilEstEnStockFaible() {
        Produit produit = new Produit(2, "Yassa poulet", "Poulet mariné citron",
                new BigDecimal("3000"), 3, 5, null, true, null);

        assertTrue(produit.isStockFaible());
        assertFalse(produit.isEnRupture());
    }
}
