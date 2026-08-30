package com.saveur221.entities;

import java.math.BigDecimal;

/**
 * Un produit contient au minimum, d'après le sujet :
 * id, libelle, description, prix, quantite_stock, categorie, disponible, image.
 *
 * Règle métier importante : si quantiteStock == 0, disponible doit être false.
 * (à faire respecter dans le Service, pas ici — l'entité reste un simple POJO)
 */
public class Produit {
    private int id;
    private String libelle;
    private String description;
    private BigDecimal prix;
    private int quantiteStock;
    private int seuilAlerte;
    private Categorie categorie;
    private boolean disponible;
    private String image;

    public Produit() {
    }

    public Produit(int id, String libelle, String description, BigDecimal prix,
                   int quantiteStock, int seuilAlerte, Categorie categorie,
                   boolean disponible, String image) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
        this.seuilAlerte = seuilAlerte;
        this.categorie = categorie;
        this.disponible = disponible;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public int getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(int quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isStockFaible() {
        return quantiteStock > 0 && quantiteStock <= seuilAlerte;
    }

    public boolean isEnRupture() {
        return quantiteStock == 0;
    }

    @Override
    public String toString() {
        return libelle + " (" + prix + " - stock: " + quantiteStock + ")";
    }
}
