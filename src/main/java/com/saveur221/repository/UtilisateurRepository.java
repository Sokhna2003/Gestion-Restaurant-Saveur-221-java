package com.saveur221.repository;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Utilisateur;
import com.saveur221.enums.RoleType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux données de la table "utilisateurs" (personnel interne : ADMIN, GERANT).
 */
public class UtilisateurRepository {

    private static final String SELECT_BASE =
            "SELECT u.id, u.nom, u.prenom, u.email, u.mot_de_passe, u.actif, r.libelle AS role_libelle " +
            "FROM utilisateurs u JOIN roles r ON u.role_id = r.id";

    public Optional<Utilisateur> findByEmail(String email) throws SQLException {
        String sql = SELECT_BASE + " WHERE u.email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Utilisateur> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE u.id = ?";
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

    public List<Utilisateur> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY u.nom";
        List<Utilisateur> utilisateurs = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                utilisateurs.add(mapRow(rs));
            }
        }
        return utilisateurs;
    }

    public List<Utilisateur> search(String motCle) throws SQLException {
        String sql = SELECT_BASE + " WHERE u.nom LIKE ? OR u.prenom LIKE ? OR u.email LIKE ? ORDER BY u.nom";
        List<Utilisateur> utilisateurs = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + motCle + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    utilisateurs.add(mapRow(rs));
                }
            }
        }
        return utilisateurs;
    }

    public Utilisateur save(Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role_id, actif) " +
                "VALUES (?, ?, ?, ?, (SELECT id FROM roles WHERE libelle = ?), ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setString(4, utilisateur.getMotDePasse());
            stmt.setString(5, utilisateur.getRole().name());
            stmt.setBoolean(6, utilisateur.isActif());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    utilisateur.setId(keys.getInt(1));
                }
            }
        }
        return utilisateur;
    }

    public void update(Utilisateur utilisateur) throws SQLException {
        String sql = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, " +
                "role_id = (SELECT id FROM roles WHERE libelle = ?), actif = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setString(4, utilisateur.getRole().name());
            stmt.setBoolean(5, utilisateur.isActif());
            stmt.setInt(6, utilisateur.getId());
            stmt.executeUpdate();
        }
    }

    public void updateMotDePasse(int id, String nouveauHash) throws SQLException {
        String sql = "UPDATE utilisateurs SET mot_de_passe = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nouveauHash);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void updateStatutActif(int id, boolean actif) throws SQLException {
        String sql = "UPDATE utilisateurs SET actif = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, actif);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public boolean existeParEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        return new Utilisateur(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                RoleType.valueOf(rs.getString("role_libelle")),
                rs.getBoolean("actif")
        );
    }
}
