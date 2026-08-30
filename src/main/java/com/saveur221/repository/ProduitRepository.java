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
 */
public class ProduitRepository {

    private static final String SELECT_BASE =
            "SELECT p.id, p.libelle, p.description, p.prix, p.quantite_stock, " +
            "p.seuil_alerte, p.disponible, p.image, " +
            "c.id AS categorie_id, c.nom AS categorie_nom, c.description AS categorie_description " +
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
        String sql = SELECT_BASE + " ORDER BY p.libelle";
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

    public Optional<Produit> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.id = ?";
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

    public List<Produit> search(String motCle) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.libelle LIKE ? ORDER BY p.libelle";
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
        String sql = SELECT_BASE + " WHERE c.id = ? ORDER BY p.libelle";
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
        String sql = SELECT_BASE + " WHERE p.disponible = ? ORDER BY p.libelle";
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
        String sql = SELECT_BASE + " WHERE p.quantite_stock = 0 ORDER BY p.libelle";
        return executeList(sql);
    }

    public List<Produit> findStockFaible() throws SQLException {
        String sql = SELECT_BASE +
                " WHERE p.quantite_stock > 0 AND p.quantite_stock <= p.seuil_alerte ORDER BY p.libelle";
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

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM produits WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
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

    private Produit mapRow(ResultSet rs) throws SQLException {
        Categorie categorie = new Categorie(
                rs.getInt("categorie_id"),
                rs.getString("categorie_nom"),
                rs.getString("categorie_description")
        );

        return new Produit(
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
    }
}
