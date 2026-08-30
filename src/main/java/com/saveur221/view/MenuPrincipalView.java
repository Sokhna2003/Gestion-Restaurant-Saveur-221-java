package com.saveur221.view;

import com.saveur221.entities.Categorie;
import com.saveur221.entities.Commande;
import com.saveur221.entities.Produit;
import com.saveur221.entities.Utilisateur;
import com.saveur221.enums.RoleType;
import com.saveur221.enums.StatutCommande;
import com.saveur221.exceptions.EntityNotFoundException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.service.CategorieService;
import com.saveur221.service.CommandeService;
import com.saveur221.service.PaiementService;
import com.saveur221.service.ProduitService;
import com.saveur221.service.StatistiqueService;
import com.saveur221.service.UtilisateurService;

import java.sql.SQLException;
import java.util.List;

/**
 * Menu principal affiché après connexion.
 * Seule classe de la couche View autorisée à appeler la couche Service
 * (contrainte du prof) : elle orchestre chaque sous-menu (gererCategories,
 * gererProduits, gererCommandes, ...) et délègue l'affichage/la saisie aux
 * vues dédiées (CategorieView, ProduitView, CommandeView, ...) qui elles
 * ne connaissent aucun Service.
 */
public class MenuPrincipalView {

    private final Utilisateur utilisateurConnecte;

    // Services
    private final CategorieService categorieService;
    private final ProduitService produitService;
    private final CommandeService commandeService;
    private final UtilisateurService utilisateurService;
    private final StatistiqueService statistiqueService;
    private final PaiementService paiementService;

    // Vues (pur affichage / saisie)
    private final CategorieView categorieView;
    private final ProduitView produitView;
    private final CommandeView commandeView;
    private final UtilisateurView utilisateurView;
    private final StatistiqueView statistiqueView;
    private final PaiementView paiementView;

    public MenuPrincipalView(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;

        this.categorieService = new CategorieService();
        this.produitService = new ProduitService();
        this.commandeService = new CommandeService();
        this.utilisateurService = new UtilisateurService();
        this.statistiqueService = new StatistiqueService();
        this.paiementService = new PaiementService();

        this.categorieView = new CategorieView();
        this.produitView = new ProduitView();
        this.commandeView = new CommandeView();
        this.utilisateurView = new UtilisateurView();
        this.statistiqueView = new StatistiqueView();
        this.paiementView = new PaiementView();
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
                case 4 -> gererCommandes();
                case 5 -> gererPaiements();
                case 6 -> gererStatistiques();
                case 7 -> {
                    if (utilisateurConnecte.getRole() == RoleType.ADMIN) {
                        gererUtilisateurs();
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

    // ==================== COMMANDES ====================

    private void gererCommandes() {
        int choix;
        do {
            choix = commandeView.demanderChoix();
            try {
                switch (choix) {
                    case 1 -> commandeView.afficherCommandes(commandeService.lister());
                    case 2 -> {
                        StatutCommande statut = commandeView.demanderStatutPourFiltre();
                        commandeView.afficherCommandes(commandeService.filtrerParStatut(statut));
                    }
                    case 3 -> {
                        String motCle = commandeView.demanderMotCle();
                        commandeView.afficherCommandes(commandeService.rechercher(motCle));
                    }
                    case 4 -> {
                        int id = commandeView.demanderId("consulter");
                        commandeView.afficherDetailCommande(commandeService.consulter(id));
                    }
                    case 5 -> {
                        int id = commandeView.demanderId("faire avancer");
                        Commande commande = commandeService.consulter(id);
                        StatutCommande[] valeurs = StatutCommande.values();

                        if (commande.getStatut().ordinal() >= StatutCommande.RETIREE.ordinal()) {
                            commandeView.afficherMessage("Cette commande est déjà terminée (" + commande.getStatut() + ").");
                        } else {
                            StatutCommande suivant = valeurs[commande.getStatut().ordinal() + 1];
                            if (commandeView.confirmerChangementStatut(commande.getStatut(), suivant)) {
                                commandeService.changerStatut(id, suivant);
                                commandeView.afficherMessage("Statut mis à jour : " + suivant);
                            }
                        }
                    }
                    case 6 -> {
                        int id = commandeView.demanderId("annuler");
                        if (commandeView.confirmerAnnulation()) {
                            commandeService.annuler(id);
                            commandeView.afficherMessage("Commande annulée, stock restitué.");
                        }
                    }
                    case 0 -> { /* retour */ }
                    default -> commandeView.afficherMessage("Option invalide.");
                }
            } catch (ValidationException | EntityNotFoundException e) {
                commandeView.afficherMessage("⚠ " + e.getMessage());
            } catch (SQLException e) {
                commandeView.afficherMessage("⚠ Erreur base de données : " + e.getMessage());
            }
        } while (choix != 0);
    }

    // ==================== UTILISATEURS (ADMIN) ====================

    private void gererUtilisateurs() {
        int choix;
        do {
            choix = utilisateurView.demanderChoix();
            try {
                switch (choix) {
                    case 1 -> {
                        Utilisateur brouillon = utilisateurView.demanderNouvelUtilisateur();
                        utilisateurService.ajouter(brouillon.getNom(), brouillon.getPrenom(),
                                brouillon.getEmail(), brouillon.getMotDePasse(), brouillon.getRole());
                        utilisateurView.afficherMessage("Utilisateur créé avec succès !");
                    }
                    case 2 -> utilisateurView.afficherUtilisateurs(utilisateurService.lister());
                    case 3 -> {
                        String motCle = utilisateurView.demanderMotCle();
                        utilisateurView.afficherUtilisateurs(utilisateurService.rechercher(motCle));
                    }
                    case 4 -> {
                        int id = utilisateurView.demanderId("modifier");
                        Utilisateur existant = utilisateurService.consulter(id);
                        Utilisateur modifie = utilisateurView.demanderModification(existant);
                        utilisateurService.modifier(id, modifie.getNom(), modifie.getPrenom(),
                                modifie.getEmail(), modifie.getRole());
                        utilisateurView.afficherMessage("Utilisateur modifié avec succès !");
                    }
                    case 5 -> {
                        int id = utilisateurView.demanderId("changer le mot de passe de");
                        String nouveauMdp = utilisateurView.demanderNouveauMotDePasse();
                        utilisateurService.changerMotDePasse(id, nouveauMdp);
                        utilisateurView.afficherMessage("Mot de passe mis à jour !");
                    }
                    case 6 -> {
                        int id = utilisateurView.demanderId("activer");
                        utilisateurService.activer(id);
                        utilisateurView.afficherMessage("Compte activé !");
                    }
                    case 7 -> {
                        int id = utilisateurView.demanderId("désactiver");
                        utilisateurService.desactiver(id, utilisateurConnecte.getId());
                        utilisateurView.afficherMessage("Compte désactivé !");
                    }
                    case 8 -> {
                        int id = utilisateurView.demanderId("supprimer");
                        if (utilisateurView.confirmerSuppression()) {
                            utilisateurService.supprimer(id, utilisateurConnecte.getId());
                            utilisateurView.afficherMessage("Utilisateur supprimé !");
                        }
                    }
                    case 0 -> { /* retour */ }
                    default -> utilisateurView.afficherMessage("Option invalide.");
                }
            } catch (ValidationException | EntityNotFoundException e) {
                utilisateurView.afficherMessage("⚠ " + e.getMessage());
            } catch (SQLException e) {
                utilisateurView.afficherMessage("⚠ Erreur base de données : " + e.getMessage());
            }
        } while (choix != 0);
    }

    // ==================== STATISTIQUES ====================

    private void gererStatistiques() {
        try {
            statistiqueView.afficherTableauDeBord(
                    statistiqueService.caDuJour(),
                    statistiqueService.caDeLaSemaine(),
                    statistiqueService.caDuMois(),
                    statistiqueService.nombreCommandes(),
                    statistiqueService.commandesEnCours(),
                    statistiqueService.commandesParStatut(),
                    statistiqueService.produitLePlusVendu(),
                    statistiqueService.topTroisProduits()
            );
        } catch (SQLException e) {
            ConsoleUtils.afficherErreur("Erreur base de données : " + e.getMessage());
        }
    }

    // ==================== PAIEMENTS ====================

    private void gererPaiements() {
        int choix;
        do {
            choix = paiementView.demanderChoix();
            try {
                switch (choix) {
                    case 1 -> paiementView.afficherCommandes(paiementService.listerImpayees());
                    case 2 -> paiementView.afficherCommandes(paiementService.listerPartiellementPayees());
                    case 3 -> {
                        int idCommande = paiementView.demanderIdCommande("payer");
                        var restant = paiementService.montantRestant(idCommande);
                        paiementView.afficherMessage("Montant restant à payer : " + restant);
                        var montant = paiementView.demanderMontant();
                        String mode = paiementView.demanderModePaiement();
                        paiementService.enregistrer(idCommande, montant, mode);
                        paiementView.afficherMessage("Paiement enregistré avec succès !");
                    }
                    case 4 -> {
                        int idCommande = paiementView.demanderIdCommande("consulter");
                        paiementView.afficherPaiements(paiementService.listerParCommande(idCommande));
                    }
                    case 0 -> { /* retour */ }
                    default -> paiementView.afficherMessage("Option invalide.");
                }
            } catch (ValidationException | EntityNotFoundException e) {
                paiementView.afficherMessage("⚠ " + e.getMessage());
            } catch (SQLException e) {
                paiementView.afficherMessage("⚠ Erreur base de données : " + e.getMessage());
            }
        } while (choix != 0);
    }

    private void afficherFonctionnaliteAVenir(String nom) {
        System.out.println("\n\"" + nom + "\" arrive dans un prochain sprint.");
    }
}
