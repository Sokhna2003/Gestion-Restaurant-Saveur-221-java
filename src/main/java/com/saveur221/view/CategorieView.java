package com.saveur221.view;

import com.saveur221.entities.Categorie;

import java.util.List;

/**
 * Vue pour la gestion des catégories : affichage et saisie uniquement.
 * Ne connaît pas CategorieService — seul MenuPrincipalView a le droit
 * d'appeler la couche Service (contrainte du prof).
 */
public class CategorieView {

    public int demanderChoix() {
        ConsoleUtils.afficherTitre("Gestion des catégories");
        System.out.println("1. Ajouter une catégorie");
        System.out.println("2. Afficher toutes les catégories");
        System.out.println("3. Rechercher une catégorie");
        System.out.println("4. Modifier une catégorie");
        System.out.println("5. Supprimer une catégorie");
        System.out.println("0. Retour au menu principal");
        return ConsoleUtils.lireEntier("Votre choix : ");
    }

    public Categorie demanderNouvelleCategorie() {
        String nom = ConsoleUtils.lireTexteObligatoire("Nom de la catégorie (obligatoire) : ");
        String description = ConsoleUtils.lireTexte("Description : ");
        return new Categorie(0, nom, description);
    }

    public int demanderId(String action) {
        return ConsoleUtils.lireEntier("Id de la catégorie à " + action + " : ");
    }

    public String demanderMotCle() {
        return ConsoleUtils.lireTexte("Mot-clé à rechercher : ");
    }

    /**
     * Redemande les champs pour une modification, en gardant l'ancienne
     * valeur si l'utilisateur laisse le champ vide (le nom reste
     * obligatoire : s'il est laissé vide, l'ancienne valeur est conservée,
     * jamais une chaîne vide).
     */
    public Categorie demanderModification(Categorie existante) {
        System.out.println("(Laisser vide pour garder la valeur actuelle)");

        String nom = ConsoleUtils.lireTexte("Nom [" + existante.getNom() + "] : ");
        if (nom.isBlank()) {
            nom = existante.getNom();
        }

        String description = ConsoleUtils.lireTexte(
                "Description [" + (existante.getDescription() == null ? "" : existante.getDescription()) + "] : ");
        if (description.isBlank()) {
            description = existante.getDescription();
        }

        return new Categorie(existante.getId(), nom, description);
    }

    public void afficherCategories(List<Categorie> categories) {
        ConsoleUtils.afficherTitre("Liste des catégories");
        if (categories.isEmpty()) {
            System.out.println("Aucune catégorie enregistrée.");
            return;
        }
        for (Categorie c : categories) {
            System.out.printf("[%d] %s - %s%n", c.getId(), c.getNom(),
                    c.getDescription() == null ? "" : c.getDescription());
        }
    }

    public void afficherCategorie(Categorie categorie) {
        if (categorie == null) {
            System.out.println("Catégorie introuvable.");
            return;
        }
        System.out.printf("[%d] %s - %s%n", categorie.getId(), categorie.getNom(),
                categorie.getDescription() == null ? "" : categorie.getDescription());
    }

    public void afficherMessage(String message) {
        System.out.println(message);
    }
}
