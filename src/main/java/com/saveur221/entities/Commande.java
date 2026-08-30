package com.saveur221.entities;

import com.saveur221.enums.StatutCommande;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private Client client;
    private LocalDateTime dateCommande;
    private StatutCommande statut;
    private BigDecimal montantTotal;
    private List<LigneCommande> lignes = new ArrayList<>();

    public Commande() {
    }

    public Commande(int id, Client client, LocalDateTime dateCommande,
                    StatutCommande statut, BigDecimal montantTotal) {
        this.id = id;
        this.client = client;
        this.dateCommande = dateCommande;
        this.statut = statut;
        this.montantTotal = montantTotal;
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

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public List<LigneCommande> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommande> lignes) {
        this.lignes = lignes;
    }

    public void ajouterLigne(LigneCommande ligne) {
        this.lignes.add(ligne);
    }

    @Override
    public String toString() {
        return "Commande #" + id + " [" + statut + "] - " + montantTotal;
    }
}
