package com.saveur221.entities;

/**
 * Objet de transport (pas une table de la base) utilisé uniquement pour
 * les statistiques : associe un produit à la quantité vendue sur la
 * période considérée.
 */
public class ProduitVendu {
    private Produit produit;
    private int quantiteVendue;

    public ProduitVendu(Produit produit, int quantiteVendue) {
        this.produit = produit;
        this.quantiteVendue = quantiteVendue;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantiteVendue() {
        return quantiteVendue;
    }
}
