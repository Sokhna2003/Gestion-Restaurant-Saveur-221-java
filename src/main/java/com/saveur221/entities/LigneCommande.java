package com.saveur221.entities;

import java.math.BigDecimal;

/**
 * Une ligne de commande fige le prix du produit au moment de l'achat
 * (prixUnitaire) : si le gérant change le prix du produit plus tard,
 * l'historique des anciennes commandes ne doit pas bouger.
 */
public class LigneCommande {
    private int id;
    private Commande commande;
    private Produit produit;
    private int quantite;
    private BigDecimal prixUnitaire;

    public LigneCommande() {
    }

    public LigneCommande(int id, Commande commande, Produit produit,
                         int quantite, BigDecimal prixUnitaire) {
        this.id = id;
        this.commande = commande;
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public BigDecimal getSousTotal() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}
