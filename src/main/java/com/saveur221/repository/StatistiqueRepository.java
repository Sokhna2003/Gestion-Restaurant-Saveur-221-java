package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Categorie;
import com.saveur221.entities.Produit;
import com.saveur221.entities.ProduitVendu;
import com.saveur221.enums.StatutCommande;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Requêtes d'agrégation pour le tableau de bord "Statistiques" du gérant.
 * Les commandes ANNULEE ne comptent ni dans le chiffre d'affaires ni dans
 * les quantités vendues : elles n'ont généré aucune vente réelle.
 */
public class StatistiqueRepository {

    public BigDecimal calculerChiffreAffaires(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) AS ca FROM commandes " +
                "WHERE statut != 'ANNULEE' AND date_commande BETWEEN ? AND ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("ca") : BigDecimal.ZERO;
            }
        }
    }

    public int compterCommandesTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM commandes";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int compterCommandesEnCours() throws SQLException {
        String sql = "SELECT COUNT(*) FROM commandes WHERE statut IN ('EN_ATTENTE', 'EN_PREPARATION', 'PRETE')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Map<StatutCommande, Integer> compterCommandesParStatut() throws SQLException {
        String sql = "SELECT statut, COUNT(*) AS nb FROM commandes GROUP BY statut";
        Map<StatutCommande, Integer> resultat = new EnumMap<>(StatutCommande.class);

        // On initialise tous les statuts à 0 pour que l'affichage soit complet
        // même si un statut n'a encore aucune commande.
        for (StatutCommande statut : StatutCommande.values()) {
            resultat.put(statut, 0);
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultat.put(StatutCommande.valueOf(rs.getString("statut")), rs.getInt("nb"));
            }
        }
        return resultat;
    }

    /**
     * Top N des produits les plus vendus (en quantité), commandes annulées exclues.
     */
    public List<ProduitVendu> topProduitsVendus(int limite) throws SQLException {
        String sql = "SELECT p.id, p.libelle, p.description, p.prix, p.quantite_stock, " +
                "p.seuil_alerte, p.disponible, p.image, " +
                "cat.id AS categorie_id, cat.nom AS categorie_nom, cat.description AS categorie_description, " +
                "SUM(lc.quantite) AS quantite_vendue " +
                "FROM ligne_commandes lc " +
                "JOIN produits p ON lc.produit_id = p.id " +
                "JOIN categories cat ON p.categorie_id = cat.id " +
                "JOIN commandes c ON lc.commande_id = c.id " +
                "WHERE c.statut != 'ANNULEE' " +
                "GROUP BY p.id " +
                "ORDER BY quantite_vendue DESC " +
                "LIMIT ?";

        List<ProduitVendu> resultat = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Categorie categorie = new Categorie(
                            rs.getInt("categorie_id"),
                            rs.getString("categorie_nom"),
                            rs.getString("categorie_description")
                    );
                    Produit produit = new Produit(
                            rs.getInt("id"),
                            rs.getString("libelle"),
                            rs.getString("description"),
                            rs.getBigDecimal("prix"),
                            rs.getInt("quantite_stock"),
                            rs.getInt("seuil_alerte"),
                            categorie,
                            rs.getBoolean("disponible"),
                            rs.getString("image")
                    );
                    resultat.add(new ProduitVendu(produit, rs.getInt("quantite_vendue")));
                }
            }
        }
        return resultat;
    }
}
