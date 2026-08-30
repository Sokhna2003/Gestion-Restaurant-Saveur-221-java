package com.saveur221.view;

import com.saveur221.entities.Categorie;
import com.saveur221.entities.Produit;
import com.saveur221.entities.Utilisateur;
import com.saveur221.enums.RoleType;
import com.saveur221.exceptions.EntityNotFoundException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.service.CategorieService;
import com.saveur221.service.ProduitService;

import java.sql.SQLException;
import java.util.List;

/**
  *  Menu principal affiché après connexion. Le GERANT et l'ADMIN partagent
 * le même espace de gestion (catégories, produits, stock, commandes,
 * paiements, statistiques) ; l'ADMIN a en plus la gestion des utilisateurs.
 *
 * Seule classe de la couche View autorisée à appeler la couche Service
 * (contrainte du prof) : elle orchestre chaque sous-menu (gererCategories,
 * gererProduits, ...) et délègue l'affichage/la saisie aux vues dédiées
 * (CategorieView, ProduitView, ...) qui elles ne connaissent aucun Service.
 */
public class MenuPrincipalView {

    private final Utilisateur utilisateurConnecte;

    // Services
    private final CategorieService categorieService;
    private final ProduitService produitService;

    // Vues (pur affichage / saisie)
    private final CategorieView categorieView;
    private final ProduitView produitView;

    public MenuPrincipalView(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;

        this.categorieService = new CategorieService();
        this.produitService = new ProduitService();

        this.categorieView = new CategorieView();
        this.produitView = new ProduitView();
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
                case 1 -> gererCategories();
                case 2 -> gererProduits();
                case 3 -> gererStock();
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

    // ==================== CATEGORIES ====================

    private void gererCategories() {
        int choix;
        do {
            choix = categorieView.demanderChoix();
            try {
                switch (choix) {
                    case 1 -> {
                        Categorie nouvelle = categorieView.demanderNouvelleCategorie();
                        categorieService.ajouter(nouvelle.getNom(), nouvelle.getDescription());
                        categorieView.afficherMessage("Catégorie ajoutée avec succès !");
                    }
                    case 2 -> categorieView.afficherCategories(categorieService.lister());
                    case 3 -> {
                        String motCle = categorieView.demanderMotCle();
                        categorieView.afficherCategories(categorieService.rechercher(motCle));
                    }
                    case 4 -> {
                        int id = categorieView.demanderId("modifier");
                        Categorie existante = categorieService.consulter(id);
                        Categorie modifiee = categorieView.demanderModification(existante);
                        categorieService.modifier(id, modifiee.getNom(), modifiee.getDescription());
                        categorieView.afficherMessage("Catégorie modifiée avec succès !");
                    }
                    case 5 -> {
                        int id = categorieView.demanderId("supprimer");
                        categorieService.supprimer(id);
                        categorieView.afficherMessage("Catégorie supprimée avec succès !");
                    }
                    case 0 -> { /* retour */ }
                    default -> categorieView.afficherMessage("Option invalide.");
                }
            } catch (ValidationException | EntityNotFoundException e) {
                categorieView.afficherMessage("⚠ " + e.getMessage());
            } catch (SQLException e) {
                categorieView.afficherMessage("⚠ Erreur base de données : " + e.getMessage());
            }
        } while (choix != 0);
    }

    // ==================== PRODUITS ====================

    private void gererProduits() {
        int choix;
        do {
            choix = produitView.demanderChoixProduits();
            try {
                switch (choix) {
                    case 1 -> {
                        List<Categorie> categories = categorieService.lister();
                        Produit brouillon = produitView.demanderNouveauProduit(categories);
                        if (brouillon != null) {
                            produitService.ajouter(brouillon.getLibelle(), brouillon.getDescription(),
                                    brouillon.getPrix(), brouillon.getQuantiteStock(), brouillon.getSeuilAlerte(),
                                    brouillon.getCategorie().getId(), brouillon.getImage());
                            produitView.afficherMessage("Produit ajouté avec succès !");
                        }
                    }
                    case 2 -> produitView.afficherProduits(produitService.lister());
                    case 3 -> {
                        String motCle = produitView.demanderMotCle();
                        produitView.afficherProduits(produitService.rechercher(motCle));
                    }
                    case 4 -> {
                        categorieView.afficherCategories(categorieService.lister());
                        int idCategorie = categorieView.demanderId("filtrer");
                        produitView.afficherProduits(produitService.filtrerParCategorie(idCategorie));
                    }
                    case 5 -> produitView.afficherProduits(produitService.listerDisponibles());
                    case 6 -> produitView.afficherProduits(produitService.listerIndisponibles());
                    case 7 -> {
                        int id = produitView.demanderId("modifier");
                        Produit existant = produitService.consulter(id);
                        List<Categorie> categories = categorieService.lister();
                        Produit modifie = produitView.demanderModification(existant, categories);
                        produitService.modifier(modifie.getId(), modifie.getLibelle(), modifie.getDescription(),
                                modifie.getPrix(), modifie.getSeuilAlerte(), modifie.getCategorie().getId(),
                                modifie.isDisponible(), modifie.getImage());
                        produitView.afficherMessage("Produit modifié avec succès !");
                    }
                    case 8 -> {
                        int id = produitView.demanderId("supprimer");
                        produitService.supprimer(id);
                        produitView.afficherMessage("Produit supprimé avec succès !");
                    }
                    case 0 -> { /* retour */ }
                    default -> produitView.afficherMessage("Option invalide.");
                }
            } catch (ValidationException | EntityNotFoundException e) {
                produitView.afficherMessage("⚠ " + e.getMessage());
            } catch (SQLException e) {
                produitView.afficherMessage("⚠ Erreur base de données : " + e.getMessage());
            }
        } while (choix != 0);
    }

    // ==================== STOCK ====================

    private void gererStock() {
        int choix;
        do {
            choix = produitView.demanderChoixStock();
            try {
                switch (choix) {
                    case 1 -> produitView.afficherProduits(produitService.lister());
                    case 2 -> {
                        int id = produitView.demanderId("approvisionner");
                        int quantite = produitView.demanderQuantiteAApprovisionner();
                        produitService.approvisionner(id, quantite);
                        produitView.afficherMessage("Stock mis à jour avec succès !");
                    }
                    case 3 -> {
                        int id = produitView.demanderId("configurer");
                        int seuil = produitView.demanderNouveauSeuil();
                        produitService.definirSeuilAlerte(id, seuil);
                        produitView.afficherMessage("Seuil d'alerte mis à jour !");
                    }
                    case 4 -> produitView.afficherProduits(produitService.listerStockFaible());
                    case 5 -> produitView.afficherProduits(produitService.listerEnRupture());
                    case 0 -> { /* retour */ }
                    default -> produitView.afficherMessage("Option invalide.");
                }
            } catch (ValidationException | EntityNotFoundException e) {
                produitView.afficherMessage("⚠ " + e.getMessage());
            } catch (SQLException e) {
                produitView.afficherMessage("⚠ Erreur base de données : " + e.getMessage());
            }
        } while (choix != 0);
    }

    private void afficherFonctionnaliteAVenir(String nom) {
        System.out.println("\n\"" + nom + "\" arrive dans un prochain sprint.");
    }
}
