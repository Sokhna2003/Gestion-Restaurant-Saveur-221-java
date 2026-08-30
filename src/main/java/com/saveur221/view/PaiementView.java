package com.saveur221.view;

import com.saveur221.entities.Commande;
import com.saveur221.entities.Paiement;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vue pour la gestion des paiements : affichage et saisie uniquement.
 * Ne connaît pas PaiementService — seul MenuPrincipalView a le droit
 * d'appeler la couche Service.
 */
public class PaiementView {

    public int demanderChoix() {
        ConsoleUtils.afficherTitre("Gestion des paiements");
        System.out.println("1. Afficher les commandes impayées");
        System.out.println("2. Afficher les commandes partiellement payées");
        System.out.println("3. Enregistrer un paiement");
        System.out.println("4. Voir l'historique des paiements d'une commande");
        System.out.println("0. Retour au menu principal");
        return ConsoleUtils.lireEntier("Votre choix : ");
    }

    public int demanderIdCommande(String action) {
        return ConsoleUtils.lireEntier("Id de la commande à " + action + " : ");
    }

    public BigDecimal demanderMontant() {
        return BigDecimal.valueOf(ConsoleUtils.lireDoublePositif("Montant du paiement : "));
    }

    public String demanderModePaiement() {
        System.out.println("Mode de paiement : 1. Espèces   2. Carte   3. Mobile Money");
        int choix = ConsoleUtils.lireEntier("Choix : ");
        return switch (choix) {
            case 1 -> "especes";
            case 2 -> "carte";
            case 3 -> "mobile_money";
            default -> "autre";
        };
    }

    public void afficherCommandes(List<Commande> commandes) {
        if (commandes.isEmpty()) {
            System.out.println("Aucune commande à afficher.");
            return;
        }
        for (Commande c : commandes) {
            System.out.printf("[%d] client: %s %s | %s | total: %.2f%n",
                    c.getId(), c.getClient().getPrenom(), c.getClient().getNom(),
                    c.getStatut(), c.getMontantTotal());
        }
    }

    public void afficherPaiements(List<Paiement> paiements) {
        if (paiements.isEmpty()) {
            System.out.println("Aucun paiement enregistré pour cette commande.");
            return;
        }
        for (Paiement p : paiements) {
            System.out.printf("[%d] %.2f - %s - %s%n",
                    p.getId(), p.getMontant(), p.getModePaiement(), p.getDatePaiement());
        }
    }

    public void afficherMessage(String message) {
        System.out.println(message);
    }
}
