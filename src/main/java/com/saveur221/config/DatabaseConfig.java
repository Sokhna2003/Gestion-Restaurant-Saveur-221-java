package com.saveur221.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gère la connexion unique (singleton) à la base de données MySQL.
 * Le Module A (Java) et le Module B (PHP) pointent vers la même base
 * "restaurant_saveur221" : ne change surtout pas son nom sans le répercuter côté PHP.
 */
public class DatabaseConfig {

    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_saveur221?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    private DatabaseConfig() {
        // Empêche l'instanciation : classe utilitaire
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL introuvable. Vérifie la dépendance mysql-connector-j.", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}