package com.saveur221.entities;

import java.time.LocalDateTime;

public class Categorie {
    private int id;
    private String nom;
    private String description;
    private String image;
    private LocalDateTime dateAjout;
    private LocalDateTime supprimeLe;

    public Categorie() {
    }

    public Categorie(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    public Categorie(int id, String nom, String description, String image) {
        this(id, nom, description);
        this.image = image;
    }

    public Categorie(int id, String nom, String description, String image,
                     LocalDateTime dateAjout, LocalDateTime supprimeLe) {
        this(id, nom, description, image);
        this.dateAjout = dateAjout;
        this.supprimeLe = supprimeLe;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    public LocalDateTime getSupprimeLe() {
        return supprimeLe;
    }

    public void setSupprimeLe(LocalDateTime supprimeLe) {
        this.supprimeLe = supprimeLe;
    }

    public boolean isSupprimee() {
        return supprimeLe != null;
    }

    @Override
    public String toString() {
        return nom;
    }
}
