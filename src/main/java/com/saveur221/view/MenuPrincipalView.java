package com.saveur221.view;

import com.saveur221.entities.Utilisateur;
import com.saveur221.enums.RoleType;

/**
 * Menu principal affiché après connexion. Le GERANT et l'ADMIN partagent
 * le même espace de gestion (catégories, produits, stock, commandes,
 * paiements, statistiques) ; l'ADMIN a en plus la gestion des utilisateurs.
 *
 * Les options ci-dessous seront branchées aux vraies vues (CategorieView,
 * ProduitView, ...) au fur et à mesure des prochains sprints.
 */
public class MenuPrincipalView {

    private final Utilisateur utilisateurConnecte;

    public MenuPrincipalView(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
    }

    public void afficher() {
        boolean quitter = false;

        while (!quitter) {
            ConsoleUtils.afficherTitre("Menu principal - " + utilisateurConnecte.getPrenom()
                    + " (" + utilisateurConnecte.getRole() + ")");

            System.out.println("1. Gestion des catégories");
            System.out.println("2. Gestion des produits");
            System.out.println("3. Gestion du stock");
            System.out.println("4. Gestion des commandes");
            System.out.println("5. Paiements");
            System.out.println("6. Statistiques");

            if (utilisateurConnecte.getRole() == RoleType.ADMIN) {
                System.out.println("7. Gestion des utilisateurs (ADMIN)");
            }

            System.out.println("0. Déconnexion");

            int choix = ConsoleUtils.lireEntier("Votre choix : ");

            switch (choix) {
                case 1 -> afficherFonctionnaliteAVenir("Gestion des catégories");
                case 2 -> afficherFonctionnaliteAVenir("Gestion des produits");
                case 3 -> afficherFonctionnaliteAVenir("Gestion du stock");
                case 4 -> afficherFonctionnaliteAVenir("Gestion des commandes");
                case 5 -> afficherFonctionnaliteAVenir("Paiements");
                case 6 -> afficherFonctionnaliteAVenir("Statistiques");
                case 7 -> {
                    if (utilisateurConnecte.getRole() == RoleType.ADMIN) {
                        afficherFonctionnaliteAVenir("Gestion des utilisateurs");
                    } else {
                        ConsoleUtils.afficherErreur("Option invalide.");
                    }
                }
                case 0 -> {
                    quitter = true;
                    System.out.println("Déconnexion réussie. À bientôt !");
                }
                default -> ConsoleUtils.afficherErreur("Option invalide.");
            }
        }
    }

    private void afficherFonctionnaliteAVenir(String nom) {
        System.out.println("\n\"" + nom + "\" arrive dans un prochain sprint.");
        ConsoleUtils.pause();
    }
}
