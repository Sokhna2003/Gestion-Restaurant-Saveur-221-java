package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Categorie;
import com.saveur221.entities.Produit;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table "produits" (JDBC pur).
 * Chaque ligne est jointe à sa catégorie pour reconstruire l'objet Categorie
 * complet plutôt que de ne garder que son id.
 * Aligné sur le Module B (PHP) : suppression en soft delete (supprime_le),
 * listes actives filtrées sur supprime_le IS NULL, corbeille (restaurer /
 * supprimer définitivement) gérée ici.
 */
public class ProduitRepository {

    private static final String SELECT_BASE =
            "SELECT p.id, p.libelle, p.description, p.prix, p.quantite_stock, " +
            "p.seuil_alerte, p.disponible, p.image, p.date_ajout, p.supprime_le, " +
            "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_description, " +
            "c.image AS categorie_image " +
            "FROM produits p " +
            "JOIN categories c ON p.categorie_id = c.id";

    public Produit save(Produit produit) throws SQLException {
        String sql = "INSERT INTO produits (libelle, description, prix, quantite_stock, " +
                "seuil_alerte, categorie_id, disponible, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produit.getLibelle());
            stmt.setString(2, produit.getDescription());
            stmt.setBigDecimal(3, produit.getPrix());
            stmt.setInt(4, produit.getQuantiteStock());
            stmt.setInt(5, produit.getSeuilAlerte());
            stmt.setInt(6, produit.getCategorie().getId());
            stmt.setBoolean(7, produit.isDisponible());
            stmt.setString(8, produit.getImage());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    produit.setId(keys.getInt(1));
                }
            }
        }
        return produit;
    }

    public List<Produit> findAll() throws SQLException {
        String sql = SELECT_BASE + " WHERE p.supprime_le IS NULL ORDER BY p.libelle";
        return executeList(sql);
    }

    public List<Produit> findAllSupprimees() throws SQLException {
        String sql = SELECT_BASE + " WHERE p.supprime_le IS NOT NULL ORDER BY p.supprime_le DESC";
        return executeList(sql);
    }

    public Optional<Produit> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.id = ? AND p.supprime_le IS NULL";
        return executeSingle(sql, id);
    }

    /**
     * Cherche un produit même s'il est en corbeille (utilisé pour restaurer
     * ou supprimer définitivement un élément de la corbeille).
     */
    public Optional<Produit> findByIdIncluantSupprime(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.id = ?";
        return executeSingle(sql, id);
    }

    public List<Produit> search(String motCle) throws SQLException {
        String sql = SELECT_BASE +
                " WHERE p.supprime_le IS NULL AND p.libelle LIKE ? ORDER BY p.libelle";
        List<Produit> produits = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + motCle + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapRow(rs));
                }
            }
        }
        return produits;
    }

    public List<Produit> findByCategorie(int categorieId) throws SQLException {
        String sql = SELECT_BASE +
                " WHERE p.supprime_le IS NULL AND c.id = ? ORDER BY p.libelle";
        List<Produit> produits = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categorieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapRow(rs));
                }
            }
        }
        return produits;
    }

    public List<Produit> findByDisponibilite(boolean disponible) throws SQLException {
        String sql = SELECT_BASE +
                " WHERE p.supprime_le IS NULL AND p.disponible = ? ORDER BY p.libelle";
        List<Produit> produits = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, disponible);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapRow(rs));
                }
            }
        }
        return produits;
    }

    public List<Produit> findEnRupture() throws SQLException {
        String sql = SELECT_BASE +
                " WHERE p.supprime_le IS NULL AND p.quantite_stock = 0 ORDER BY p.libelle";
        return executeList(sql);
    }

    public List<Produit> findStockFaible() throws SQLException {
        String sql = SELECT_BASE +
                " WHERE p.supprime_le IS NULL AND p.quantite_stock > 0 " +
                "AND p.quantite_stock <= p.seuil_alerte ORDER BY p.libelle";
        return executeList(sql);
    }

    public void update(Produit produit) throws SQLException {
        String sql = "UPDATE produits SET libelle = ?, description = ?, prix = ?, " +
                "quantite_stock = ?, seuil_alerte = ?, categorie_id = ?, disponible = ?, image = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produit.getLibelle());
            stmt.setString(2, produit.getDescription());
            stmt.setBigDecimal(3, produit.getPrix());
            stmt.setInt(4, produit.getQuantiteStock());
            stmt.setInt(5, produit.getSeuilAlerte());
            stmt.setInt(6, produit.getCategorie().getId());
            stmt.setBoolean(7, produit.isDisponible());
            stmt.setString(8, produit.getImage());
            stmt.setInt(9, produit.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Met à jour uniquement la quantité en stock (utilisé lors d'une commande,
     * d'un approvisionnement ou d'une annulation). La règle "disponible = false
     * si stock = 0" est appliquée ici directement.
     */
    public void updateStock(int produitId, int nouvelleQuantite) throws SQLException {
        String sql = "UPDATE produits SET quantite_stock = ?, disponible = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nouvelleQuantite);
            stmt.setBoolean(2, nouvelleQuantite > 0);
            stmt.setInt(3, produitId);
            stmt.executeUpdate();
        }
    }

    /**
     * Soft delete : déplace le produit dans la corbeille (supprime_le = date).
     */
    public void delete(int id) throws SQLException {
        String sql = "UPDATE produits SET supprime_le = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void restaurer(int id) throws SQLException {
        String sql = "UPDATE produits SET supprime_le = NULL WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Suppression physique (définitive). A ne déclencher que sur un produit
     * déjà en corbeille et sans ligne de commande liée, sinon violation de la
     * clé étrangère fk_ligne_produit.
     */
    public void supprimerDefinitivement(int id) throws SQLException {
        String sql = "DELETE FROM produits WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Vérifie si le produit apparaît dans l'historique des commandes
     * (ligne_commandes) : dans ce cas on ne peut pas le supprimer
     * définitivement (clé étrangère fk_ligne_produit).
     */
    public boolean estReferenceDansDesCommandes(int produitId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ligne_commandes WHERE produit_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produitId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private List<Produit> executeList(String sql) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                produits.add(mapRow(rs));
            }
        }
        return produits;
    }

    private Optional<Produit> executeSingle(String sql, int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Produit mapRow(ResultSet rs) throws SQLException {
        Categorie categorie = new Categorie(
                rs.getInt("categorie_id"),
                rs.getString("categorie_nom"),
                rs.getString("categorie_description"),
                rs.getString("categorie_image")
        );

        Timestamp dateAjout = rs.getTimestamp("date_ajout");
        Timestamp supprimeLe = rs.getTimestamp("supprime_le");
        return new Produit(
                rs.getInt("id"),
                rs.getString("libelle"),
                rs.getString("description"),
                rs.getBigDecimal("prix"),
                rs.getInt("quantite_stock"),
                rs.getInt("seuil_alerte"),
                categorie,
                rs.getBoolean("disponible"),
                rs.getString("image"),
                dateAjout != null ? dateAjout.toLocalDateTime() : null,
                supprimeLe != null ? supprimeLe.toLocalDateTime() : null
        );
    }
}