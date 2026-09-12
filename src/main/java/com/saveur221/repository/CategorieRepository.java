package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Categorie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table "categories" (JDBC pur, sans framework ORM).
 * Aligné sur le Module B (PHP) : la suppression est un soft delete
 * (supprime_le = date), les listes actives filtrent supprime_le IS NULL et
 * la corbeille (restaurer / supprimer définitivement) est gérée ici.
 */
public class CategorieRepository {

    private static final String SELECT_COLONNES =
            "id, nom, description, image, date_ajout, supprime_le";

    public Categorie save(Categorie categorie) throws SQLException {
        String sql = "INSERT INTO categories (nom, description, image) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, categorie.getNom());
            stmt.setString(2, categorie.getDescription());
            stmt.setString(3, categorie.getImage());
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
        String sql = "SELECT " + SELECT_COLONNES + " FROM categories " +
                "WHERE supprime_le IS NULL ORDER BY nom";
        return findMany(sql, List.of());
    }

    public List<Categorie> findMany(String sql, List<Object> parametres) throws SQLException {
        List<Categorie> categories = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parametres.size(); i++) {
                stmt.setObject(i + 1, parametres.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapRow(rs));
                }
            }
        }
        return categories;
    }

    public Optional<Categorie> findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLONNES + " FROM categories " +
                "WHERE id = ? AND supprime_le IS NULL";
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

    /**
     * Cherche une catégorie même si elle est en corbeille (utilisé pour
     * restaurer ou supprimer définitivement un élément de la corbeille).
     */
    public Optional<Categorie> findByIdIncluantSupprimee(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLONNES + " FROM categories WHERE id = ?";
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
        String sql = "SELECT " + SELECT_COLONNES + " FROM categories " +
                "WHERE supprime_le IS NULL AND (nom LIKE ? OR description LIKE ?) ORDER BY nom";
        String pattern = "%" + motCle + "%";
        return findMany(sql, List.of(pattern, pattern));
    }

    public void update(Categorie categorie) throws SQLException {
        String sql = "UPDATE categories SET nom = ?, description = ?, image = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categorie.getNom());
            stmt.setString(2, categorie.getDescription());
            stmt.setString(3, categorie.getImage());
            stmt.setInt(4, categorie.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Soft delete : déplace la catégorie dans la corbeille (supprime_le = date).
     */
    public void delete(int id) throws SQLException {
        String sql = "UPDATE categories SET supprime_le = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Categorie> findAllSupprimees() throws SQLException {
        String sql = "SELECT " + SELECT_COLONNES + " FROM categories " +
                "WHERE supprime_le IS NOT NULL ORDER BY supprime_le DESC";
        return findMany(sql, List.of());
    }

    public void restaurer(int id) throws SQLException {
        String sql = "UPDATE categories SET supprime_le = NULL WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Suppression physique (définitive). A ne déclencher que sur une
     * catégorie déjà en corbeille et sans produit lié (y compris en
     * corbeille), sinon violation de la clé étrangère fk_produit_categorie.
     */
    public void supprimerDefinitivement(int id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Compte les produits liés à la catégorie. Le paramètre avecCorbeille
     * permet d'inclure les produits eux-mêmes en corbeille (Utilisé pour la
     * suppression définitive : ils référencent encore la catégorie).
     */
    public int compterProduits(int categorieId, boolean avecCorbeille) throws SQLException {
        String sql = "SELECT COUNT(*) FROM produits WHERE categorie_id = ?";
        if (!avecCorbeille) {
            sql += " AND supprime_le IS NULL";
        }
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categorieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Categorie mapRow(ResultSet rs) throws SQLException {
        Timestamp dateAjout = rs.getTimestamp("date_ajout");
        Timestamp supprimeLe = rs.getTimestamp("supprime_le");
        return new Categorie(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getString("image"),
                dateAjout != null ? dateAjout.toLocalDateTime() : null,
                supprimeLe != null ? supprimeLe.toLocalDateTime() : null
        );
    }
}