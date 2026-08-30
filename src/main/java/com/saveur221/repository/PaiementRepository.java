package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Client;
import com.saveur221.entities.Commande;
import com.saveur221.entities.Paiement;
import com.saveur221.enums.StatutCommande;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux données de la table "paiements" (JDBC pur).
 * Règle métier : total des paiements d'une commande <= montant de la
 * commande (vérifiée par PaiementService, pas ici).
 */
public class PaiementRepository {

    private static final String SELECT_COMMANDE_HEADER =
            "c.id AS cmd_id, c.date_commande, c.statut, c.montant_total, " +
            "cl.id AS client_id, cl.nom AS client_nom, cl.prenom AS client_prenom, " +
            "cl.email AS client_email, cl.telephone AS client_telephone, cl.adresse AS client_adresse " +
            "FROM commandes c JOIN clients cl ON c.client_id = cl.id";

    public BigDecimal montantDejaPaye(int commandeId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant), 0) AS total FROM paiements WHERE commande_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commandeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
            }
        }
    }

    public Paiement save(Paiement paiement) throws SQLException {
        String sql = "INSERT INTO paiements (commande_id, montant, mode_paiement) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, paiement.getCommande().getId());
            stmt.setBigDecimal(2, paiement.getMontant());
            stmt.setString(3, paiement.getModePaiement());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    paiement.setId(keys.getInt(1));
                }
            }
        }
        return paiement;
    }

    public List<Paiement> findByCommande(int commandeId) throws SQLException {
        String sql = "SELECT id, montant, date_paiement, mode_paiement FROM paiements " +
                "WHERE commande_id = ? ORDER BY date_paiement";
        List<Paiement> paiements = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commandeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("date_paiement");
                    paiements.add(new Paiement(
                            rs.getInt("id"),
                            null, // commande non rechargée ici, pas nécessaire pour cet affichage
                            rs.getBigDecimal("montant"),
                            ts != null ? ts.toLocalDateTime() : null,
                            rs.getString("mode_paiement")
                    ));
                }
            }
        }
        return paiements;
    }

    public List<Commande> findCommandesImpayees() throws SQLException {
        String sql = "SELECT " + SELECT_COMMANDE_HEADER +
                " LEFT JOIN paiements p ON p.commande_id = c.id " +
                "WHERE c.statut != 'ANNULEE' " +
                "GROUP BY c.id " +
                "HAVING COALESCE(SUM(p.montant), 0) = 0";
        return executerRequeteCommandes(sql);
    }

    public List<Commande> findCommandesPartiellementPayees() throws SQLException {
        String sql = "SELECT " + SELECT_COMMANDE_HEADER +
                " LEFT JOIN paiements p ON p.commande_id = c.id " +
                "WHERE c.statut != 'ANNULEE' " +
                "GROUP BY c.id " +
                "HAVING COALESCE(SUM(p.montant), 0) > 0 AND COALESCE(SUM(p.montant), 0) < c.montant_total";
        return executerRequeteCommandes(sql);
    }

    private List<Commande> executerRequeteCommandes(String sql) throws SQLException {
        List<Commande> commandes = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Client client = new Client(
                        rs.getInt("client_id"),
                        rs.getString("client_nom"),
                        rs.getString("client_prenom"),
                        rs.getString("client_email"),
                        rs.getString("client_telephone"),
                        rs.getString("client_adresse"),
                        null
                );
                Timestamp ts = rs.getTimestamp("date_commande");
                commandes.add(new Commande(
                        rs.getInt("cmd_id"),
                        client,
                        ts != null ? ts.toLocalDateTime() : null,
                        StatutCommande.valueOf(rs.getString("statut")),
                        rs.getBigDecimal("montant_total")
                ));
            }
        }
        return commandes;
    }
}
