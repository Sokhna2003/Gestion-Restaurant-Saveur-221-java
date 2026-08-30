package com.saveur221.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Règle métier du sujet : total des paiements d'une commande <= montant de la commande.
 * Cette règle sera vérifiée dans le Service, pas ici.
 */
public class Paiement {
    private int id;
    private Commande commande;
    private BigDecimal montant;
    private LocalDateTime datePaiement;
    private String modePaiement; // ex: "especes", "carte", "mobile_money"

    public Paiement() {
    }

    public Paiement(int id, Commande commande, BigDecimal montant,
                    LocalDateTime datePaiement, String modePaiement) {
        this.id = id;
        this.commande = commande;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.modePaiement = modePaiement;
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

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }
}
