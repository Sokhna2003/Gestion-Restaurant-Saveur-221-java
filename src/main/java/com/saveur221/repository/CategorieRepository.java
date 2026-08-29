package src.main.java.com.saveur221.repository;

import src.main.java.com.saveur221.config.DatabaseConfig;
import src.main.java.com.saveur221.entities.Categorie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table "categories" (JDBC pur, sans framework ORM).
 * La règle métier "une catégorie contenant des produits ne peut pas être
 * supprimée" est déjà garantie par la contrainte SQL ON DELETE RESTRICT
 * (voir script.sql) : delete() laissera remonter la SQLException dans ce cas,
 * à la couche Service de la traduire en ValidationException.
 */
public class CategorieRepository {

    public Categorie save(Categorie categorie) throws SQLException {
        String sql = "INSERT INTO categories (nom, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, categorie.getNom());
            stmt.setString(2, categorie.getDescription());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    categorie.setId(keys.getInt(1));
                }
            }
        }
        return categorie;
    }

    public List<Categorie> findAll() throws SQLException {
        String sql = "SELECT id, nom, description FROM categories ORDER BY nom";
        List<Categorie> categories = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        }
        return categories;
    }

    public Optional<Categorie> findById(int id) throws SQLException {
        String sql = "SELECT id, nom, description FROM categories WHERE id = ?";
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

    public List<Categorie> search(String motCle) throws SQLException {
        String sql = "SELECT id, nom, description FROM categories WHERE nom LIKE ? ORDER BY nom";
        List<Categorie> categories = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + motCle + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapRow(rs));
                }
            }
        }
        return categories;
    }

    public void update(Categorie categorie) throws SQLException {
        String sql = "UPDATE categories SET nom = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categorie.getNom());
            stmt.setString(2, categorie.getDescription());
            stmt.setInt(3, categorie.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Peut lever une SQLException (code MySQL 1451) si des produits
     * référencent encore cette catégorie : c'est la contrainte
     * ON DELETE RESTRICT du script.sql qui protège la règle métier.
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public boolean estUtiliseeParDesProduits(int categorieId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM produits WHERE categorie_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categorieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Categorie mapRow(ResultSet rs) throws SQLException {
        return new Categorie(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description")
        );
    }
}
