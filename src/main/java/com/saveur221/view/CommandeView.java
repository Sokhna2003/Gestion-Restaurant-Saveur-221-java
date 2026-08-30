package com.saveur221.view;

import com.saveur221.entities.Commande;
import com.saveur221.entities.LigneCommande;
import com.saveur221.enums.StatutCommande;

import java.util.List;

/**
 * Vue pour la gestion des commandes (côté gérant/admin) : affichage et
 * saisie uniquement. Ne connaît pas CommandeService — seul
 * MenuPrincipalView a le droit d'appeler la couche Service.
 */
public class CommandeView {

    public int demanderChoix() {
        ConsoleUtils.afficherTitre("Gestion des commandes");
        System.out.println("1. Afficher toutes les commandes");
        System.out.println("2. Filtrer par statut");
        System.out.println("3. Rechercher (nom client ou n° commande)");
        System.out.println("4. Consulter le détail d'une commande");
        System.out.println("5. Faire avancer le statut d'une commande");
        System.out.println("6. Annuler une commande");
        System.out.println("0. Retour au menu principal");
        return ConsoleUtils.lireEntier("Votre choix : ");
    }

    public int demanderId(String action) {
        return ConsoleUtils.lireEntier("Id de la commande à " + action + " : ");
    }

    public String demanderMotCle() {
        return ConsoleUtils.lireTexte("Nom du client ou n° de commande : ");
    }

    public StatutCommande demanderStatutPourFiltre() {
        StatutCommande[] valeurs = StatutCommande.values();
        System.out.println("Statuts disponibles :");
        for (int i = 0; i < valeurs.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, valeurs[i]);
        }
        int choix = ConsoleUtils.lireEntier("Choix : ");
        while (choix < 1 || choix > valeurs.length) {
            System.out.println("Choix invalide.");
            choix = ConsoleUtils.lireEntier("Choix : ");
        }
        return valeurs[choix - 1];
    }

    /**
     * Demande confirmation pour faire avancer une commande vers l'étape
     * suivante. La logique de calcul de "l'étape suivante" reste dans
     * MenuPrincipalView (qui seul connaît le Service et ses règles).
     */
    public boolean confirmerChangementStatut(StatutCommande actuel, StatutCommande suivant) {
        String reponse = ConsoleUtils.lireTexte(
                "Faire passer la commande de " + actuel + " à " + suivant + " ? (o/N) : ");
        return reponse.equalsIgnoreCase("o");
    }

    public boolean confirmerAnnulation() {
        String reponse = ConsoleUtils.lireTexte(
                "Confirmer l'annulation ? Le stock sera restitué. (o/N) : ");
        return reponse.equalsIgnoreCase("o");
    }

    public void afficherCommandes(List<Commande> commandes) {
        if (commandes.isEmpty()) {
            System.out.println("Aucune commande à afficher.");
            return;
        }
        for (Commande c : commandes) {
            System.out.printf("[%d] %s | client: %s %s | %s | total: %.2f%n",
                    c.getId(), c.getDateCommande(), c.getClient().getPrenom(), c.getClient().getNom(),
                    c.getStatut(), c.getMontantTotal());
        }
    }

    public void afficherDetailCommande(Commande commande) {
        if (commande == null) {
            System.out.println("Commande introuvable.");
            return;
        }

        System.out.println("Commande #" + commande.getId() + " - " + commande.getStatut());
        System.out.println("Client : " + commande.getClient().getPrenom() + " " + commande.getClient().getNom());
        System.out.println("Date : " + commande.getDateCommande());
        System.out.println("Lignes :");

        for (LigneCommande ligne : commande.getLignes()) {
            System.out.printf("  - %s x%d @ %.2f = %.2f%n",
                    ligne.getProduit().getLibelle(), ligne.getQuantite(),
                    ligne.getPrixUnitaire(), ligne.getSousTotal());
        }

        System.out.printf("Total : %.2f%n", commande.getMontantTotal());
    }

    public void afficherMessage(String message) {
        System.out.println(message);
    }
}