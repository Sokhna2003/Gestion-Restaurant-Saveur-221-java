package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.*;
import com.saveur221.enums.StatutCommande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données des tables "commandes" et "ligne_commandes" (JDBC pur).
 * Le Module A (Java) ne crée pas de commandes (c'est le rôle du Module B PHP
 * côté client) : il les consulte, les filtre et change leur statut.
 */
public class CommandeRepository {

    private static final String SELECT_COMMANDE_BASE =
            "SELECT c.id, c.date_commande, c.statut, c.montant_total, " +
            "cl.id AS client_id, cl.nom AS client_nom, cl.prenom AS client_prenom, " +
            "cl.email AS client_email, cl.telephone AS client_telephone, cl.adresse AS client_adresse " +
            "FROM commandes c JOIN clients cl ON c.client_id = cl.id";

    private static final String SELECT_LIGNE_BASE =
            "SELECT lc.id, lc.quantite, lc.prix_unitaire, " +
            "p.id AS produit_id, p.libelle, p.description, p.prix, p.quantite_stock, " +
            "p.seuil_alerte, p.disponible, p.image, " +
            "cat.id AS categorie_id, cat.nom AS categorie_nom, cat.description AS categorie_description " +
            "FROM ligne_commandes lc " +
            "JOIN produits p ON lc.produit_id = p.id " +
            "JOIN categories cat ON p.categorie_id = cat.id " +
            "WHERE lc.commande_id = ?";

    public List<Commande> findAll() throws SQLException {
        String sql = SELECT_COMMANDE_BASE + " ORDER BY c.date_commande DESC";
        List<Commande> commandes = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                commandes.add(mapCommandeSansLignes(rs));
            }
        }
        return commandes;
    }

    public List<Commande> findByStatut(StatutCommande statut) throws SQLException {
        String sql = SELECT_COMMANDE_BASE + " WHERE c.statut = ? ORDER BY c.date_commande DESC";
        List<Commande> commandes = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    commandes.add(mapCommandeSansLignes(rs));
                }
            }
        }
        return commandes;
    }

    public List<Commande> search(String motCle) throws SQLException {
        String sql = SELECT_COMMANDE_BASE +
                " WHERE cl.nom LIKE ? OR cl.prenom LIKE ? OR c.id = ? ORDER BY c.date_commande DESC";
        List<Commande> commandes = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + motCle + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            // si motCle n'est pas un nombre, on compare à -1 (ne matchera jamais un id)
            int idPossible;
            try {
                idPossible = Integer.parseInt(motCle.trim());
            } catch (NumberFormatException e) {
                idPossible = -1;
            }
            stmt.setInt(3, idPossible);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    commandes.add(mapCommandeSansLignes(rs));
                }
            }
        }
        return commandes;
    }

    /**
     * Charge une commande avec le détail complet de ses lignes (produits inclus).
     */
    public Optional<Commande> findByIdAvecLignes(int id) throws SQLException {
        String sql = SELECT_COMMANDE_BASE + " WHERE c.id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Commande commande = mapCommandeSansLignes(rs);
                commande.setLignes(findLignesByCommande(id, commande));
                return Optional.of(commande);
            }
        }
    }

    public List<LigneCommande> findLignesByCommande(int commandeId, Commande commande) throws SQLException {
        List<LigneCommande> lignes = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_LIGNE_BASE)) {

            stmt.setInt(1, commandeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Categorie categorie = new Categorie(
                            rs.getInt("categorie_id"),
                            rs.getString("categorie_nom"),
                            rs.getString("categorie_description")
                    );
                    Produit produit = new Produit(
                            rs.getInt("produit_id"),
                            rs.getString("libelle"),
                            rs.getString("description"),
                            rs.getBigDecimal("prix"),
                            rs.getInt("quantite_stock"),
                            rs.getInt("seuil_alerte"),
                            categorie,
                            rs.getBoolean("disponible"),
                            rs.getString("image")
                    );
                    LigneCommande ligne = new LigneCommande(
                            rs.getInt("id"),
                            commande,
                            produit,
                            rs.getInt("quantite"),
                            rs.getBigDecimal("prix_unitaire")
                    );
                    lignes.add(ligne);
                }
            }
        }
        return lignes;
    }

    /**
     * Change uniquement le statut d'une commande. La validation de la
     * transition (ex: on ne repasse pas de RETIREE à EN_ATTENTE) est faite
     * en amont par CommandeService.
     */
    public void updateStatut(int commandeId, StatutCommande nouveauStatut) throws SQLException {
        String sql = "UPDATE commandes SET statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nouveauStatut.name());
            stmt.setInt(2, commandeId);
            stmt.executeUpdate();
        }
    }

    private Commande mapCommandeSansLignes(ResultSet rs) throws SQLException {
        Client client = new Client(
                rs.getInt("client_id"),
                rs.getString("client_nom"),
                rs.getString("client_prenom"),
                rs.getString("client_email"),
                rs.getString("client_telephone"),
                rs.getString("client_adresse"),
                null // mot de passe non chargé ici, inutile pour cette vue
        );

        Timestamp timestamp = rs.getTimestamp("date_commande");
        return new Commande(
                rs.getInt("id"),
                client,
                timestamp != null ? timestamp.toLocalDateTime() : null,
                StatutCommande.valueOf(rs.getString("statut")),
                rs.getBigDecimal("montant_total")
        );
    }
}