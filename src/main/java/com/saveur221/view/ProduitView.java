package com.saveur221.view;

import com.saveur221.entities.Categorie;
import com.saveur221.entities.Produit;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vue pour la gestion des produits et du stock : affichage et saisie
 * uniquement. Ne connaît pas ProduitService — seul MenuPrincipalView a le
 * droit d'appeler la couche Service (contrainte du prof).
 */
public class ProduitView {

    public int demanderChoixProduits() {
        ConsoleUtils.afficherTitre("Gestion des produits");
        System.out.println("1. Ajouter un produit");
        System.out.println("2. Afficher tous les produits");
        System.out.println("3. Rechercher un produit par libellé");
        System.out.println("4. Filtrer par catégorie");
        System.out.println("5. Voir les produits disponibles");
        System.out.println("6. Voir les produits indisponibles");
        System.out.println("7. Modifier un produit");
        System.out.println("8. Supprimer un produit");
        System.out.println("0. Retour au menu principal");
        return ConsoleUtils.lireEntier("Votre choix : ");
    }

    public int demanderChoixStock() {
        ConsoleUtils.afficherTitre("Gestion du stock");
        System.out.println("1. Consulter le stock de tous les produits");
        System.out.println("2. Approvisionner un produit");
        System.out.println("3. Définir le seuil d'alerte d'un produit");
        System.out.println("4. Voir les produits en stock faible");
        System.out.println("5. Voir les produits en rupture");
        System.out.println("0. Retour au menu principal");
        return ConsoleUtils.lireEntier("Votre choix : ");
    }

    /**
     * Libellé, prix, quantité et catégorie sont obligatoires ; description,
     * image et seuil d'alerte restent libres (seuil à 0 par défaut si
     * laissé vide n'est pas géré ici : on redemande un entier positif ou nul).
     */
    public Produit demanderNouveauProduit(List<Categorie> categoriesDisponibles) {
        String libelle = ConsoleUtils.lireTexteObligatoire("Libellé (obligatoire) : ");
        String description = ConsoleUtils.lireTexte("Description : ");
        BigDecimal prix = BigDecimal.valueOf(ConsoleUtils.lireDoublePositif("Prix (obligatoire) : "));
        int quantiteStock = ConsoleUtils.lireEntierPositifOuNul("Quantité en stock (obligatoire) : ");
        int seuilAlerte = ConsoleUtils.lireEntierPositifOuNul("Seuil d'alerte (obligatoire) : ");
        String image = ConsoleUtils.lireTexte("Chemin/URL de l'image (optionnel) : ");

        Categorie categorie = demanderSelectionCategorieObligatoire(categoriesDisponibles);
        if (categorie == null) {
            return null;
        }

        boolean disponible = quantiteStock > 0;
        return new Produit(0, libelle, description, prix, quantiteStock, seuilAlerte,
                categorie, disponible, image);
    }

    public Produit demanderModification(Produit existant, List<Categorie> categoriesDisponibles) {
        System.out.println("(Laisser vide pour garder la valeur actuelle)");

        String libelle = ConsoleUtils.lireTexte("Libellé [" + existant.getLibelle() + "] : ");
        if (libelle.isBlank()) libelle = existant.getLibelle();

        String description = ConsoleUtils.lireTexte(
                "Description [" + (existant.getDescription() == null ? "" : existant.getDescription()) + "] : ");
        if (description.isBlank()) description = existant.getDescription();

        String prixSaisi = ConsoleUtils.lireTexte("Prix [" + existant.getPrix() + "] : ");
        BigDecimal prix = prixSaisi.isBlank() ? existant.getPrix() : BigDecimal.valueOf(Double.parseDouble(prixSaisi));

        String seuilSaisi = ConsoleUtils.lireTexte("Seuil d'alerte [" + existant.getSeuilAlerte() + "] : ");
        int seuilAlerte = seuilSaisi.isBlank() ? existant.getSeuilAlerte() : Integer.parseInt(seuilSaisi);

        String image = ConsoleUtils.lireTexte(
                "Image [" + (existant.getImage() == null ? "" : existant.getImage()) + "] : ");
        if (image.isBlank()) image = existant.getImage();

        System.out.println("Changer la catégorie actuelle (" + existant.getCategorie().getNom() + ") ? (o/N)");
        String reponse = ConsoleUtils.lireTexte("> ");
        Categorie categorie = existant.getCategorie();
        if (reponse.equalsIgnoreCase("o")) {
            Categorie nouvelle = demanderSelectionCategorieObligatoire(categoriesDisponibles);
            if (nouvelle != null) {
                categorie = nouvelle;
            }
        }

        return new Produit(existant.getId(), libelle, description, prix, existant.getQuantiteStock(),
                seuilAlerte, categorie, existant.isDisponible(), image);
    }

    /**
     * Redemande tant que l'id saisi ne correspond à aucune catégorie de la
     * liste. Retourne null uniquement si la liste elle-même est vide
     * (impossible de choisir quoi que ce soit).
     */
    private Categorie demanderSelectionCategorieObligatoire(List<Categorie> categoriesDisponibles) {
        if (categoriesDisponibles.isEmpty()) {
            System.out.println("Aucune catégorie disponible. Crée d'abord une catégorie.");
            return null;
        }

        while (true) {
            System.out.println("Catégories disponibles :");
            for (Categorie c : categoriesDisponibles) {
                System.out.printf("  [%d] %s%n", c.getId(), c.getNom());
            }

            int idChoisi = ConsoleUtils.lireEntier("Id de la catégorie (obligatoire) : ");
            Categorie trouvee = categoriesDisponibles.stream()
                    .filter(c -> c.getId() == idChoisi)
                    .findFirst()
                    .orElse(null);

            if (trouvee != null) {
                return trouvee;
            }
            System.out.println("Id de catégorie invalide, réessaie.");
        }
    }

    public int demanderId(String action) {
        return ConsoleUtils.lireEntier("Id du produit à " + action + " : ");
    }

    public String demanderMotCle() {
        return ConsoleUtils.lireTexte("Mot-clé à rechercher : ");
    }

    public int demanderQuantiteAApprovisionner() {
        return ConsoleUtils.lireEntierPositifOuNul("Quantité à ajouter au stock (obligatoire) : ");
    }

    public int demanderNouveauSeuil() {
        return ConsoleUtils.lireEntierPositifOuNul("Nouveau seuil d'alerte (obligatoire) : ");
    }

    public void afficherProduits(List<Produit> produits) {
        if (produits.isEmpty()) {
            System.out.println("Aucun produit à afficher.");
            return;
        }
        for (Produit p : produits) {
            afficherLigneProduit(p);
        }
    }

    public void afficherProduit(Produit produit) {
        if (produit == null) {
            System.out.println("Produit introuvable.");
            return;
        }
        afficherLigneProduit(produit);
    }

    private void afficherLigneProduit(Produit p) {
        System.out.printf("[%d] %s | %.2f | stock: %d (seuil: %d) | %s | %s | %s%n",
                p.getId(), p.getLibelle(), p.getPrix(), p.getQuantiteStock(), p.getSeuilAlerte(),
                p.getCategorie().getNom(),
                p.isDisponible() ? "disponible" : "indisponible",
                p.isEnRupture() ? "RUPTURE" : (p.isStockFaible() ? "stock faible" : "stock ok"));
    }

    public void afficherMessage(String message) {
        System.out.println(message);
    }
}
