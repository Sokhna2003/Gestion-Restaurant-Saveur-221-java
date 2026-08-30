package com.saveur221.entities;

import java.time.LocalDateTime;

/**
 * Un avis est laissé par un Client sur une Commande, uniquement après
 * qu'elle soit passée au statut RETIREE (règle métier du sujet).
 * Un seul avis par commande.
 */
public class Avis {
    private int id;
    private Client client;
    private Commande commande;
    private int note; // de 1 à 5
    private String commentaire;
    private LocalDateTime dateAvis;

    public Avis() {
    }

    public Avis(int id, Client client, Commande commande, int note,
               String commentaire, LocalDateTime dateAvis) {
        this.id = id;
        this.client = client;
        this.commande = commande;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(LocalDateTime dateAvis) {
        this.dateAvis = dateAvis;
    }
}
