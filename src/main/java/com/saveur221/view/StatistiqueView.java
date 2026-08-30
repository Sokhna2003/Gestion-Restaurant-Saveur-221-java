package com.saveur221.view;

import com.saveur221.entities.ProduitVendu;
import com.saveur221.enums.StatutCommande;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Vue du tableau de bord "Statistiques" : affichage uniquement. Ne connaît
 * pas StatistiqueService — reçoit toutes les valeurs déjà calculées par
 * MenuPrincipalView (seule classe autorisée à appeler la couche Service).
 */
public class StatistiqueView {

    public void afficherTableauDeBord(BigDecimal caJour, BigDecimal caSemaine, BigDecimal caMois,
                                       int nombreCommandes, int commandesEnCours,
                                       Map<StatutCommande, Integer> commandesParStatut,
                                       ProduitVendu produitLePlusVendu,
                                       List<ProduitVendu> topTroisProduits) {

        ConsoleUtils.afficherTitre("Statistiques");

        System.out.printf("Chiffre d'affaires du jour    : %.2f%n", caJour);
        System.out.printf("Chiffre d'affaires de la semaine : %.2f%n", caSemaine);
        System.out.printf("Chiffre d'affaires du mois     : %.2f%n", caMois);

        System.out.println();
        System.out.println("Nombre total de commandes : " + nombreCommandes);
        System.out.println("Commandes en cours (en attente/préparation/prête) : " + commandesEnCours);

        System.out.println();
        System.out.println("Répartition des commandes par statut :");
        for (Map.Entry<StatutCommande, Integer> entry : commandesParStatut.entrySet()) {
            System.out.printf("  - %s : %d%n", entry.getKey(), entry.getValue());
        }

        System.out.println();
        if (produitLePlusVendu == null) {
            System.out.println("Produit le plus vendu : aucune vente enregistrée pour le moment.");
        } else {
            System.out.printf("Produit le plus vendu : %s (%d unités vendues)%n",
                    produitLePlusVendu.getProduit().getLibelle(), produitLePlusVendu.getQuantiteVendue());
        }

        System.out.println();
        System.out.println("Top 3 des produits :");
        if (topTroisProduits.isEmpty()) {
            System.out.println("  Aucune vente enregistrée pour le moment.");
        } else {
            int rang = 1;
            for (ProduitVendu pv : topTroisProduits) {
                System.out.printf("  %d. %s - %d unités vendues%n",
                        rang++, pv.getProduit().getLibelle(), pv.getQuantiteVendue());
            }
        }
    }
}
