package com.saveur221.view;

import com.saveur221.entities.Utilisateur;
import com.saveur221.exceptions.AuthentificationException;
import com.saveur221.service.AuthService;

import java.sql.SQLException;

/**
 * Écran de connexion, obligatoire avant tout accès à l'application
 * (exigence du sujet). Réessaie jusqu'à connexion réussie ou choix
 * explicite de quitter.
 */
public class LoginView {

    private final AuthService authService = new AuthService();

    /**
     * @return l'utilisateur connecté, ou null si l'utilisateur choisit de quitter
     */
    public Utilisateur afficher() {
        ConsoleUtils.afficherTitre("Connexion - Saveur 221");

        while (true) {
            String email = ConsoleUtils.lireTexte("Email (ou 'q' pour quitter) : ");
            if (email.equalsIgnoreCase("q")) {
                return null;
            }

            String motDePasse = ConsoleUtils.lireTexte("Mot de passe : ");

            try {
                Utilisateur utilisateur = authService.login(email, motDePasse);
                System.out.println("\nBienvenue, " + utilisateur.getPrenom() + " (" + utilisateur.getRole() + ") !");
                return utilisateur;
            } catch (AuthentificationException e) {
                ConsoleUtils.afficherErreur(e.getMessage());
            } catch (SQLException e) {
                ConsoleUtils.afficherErreur("Erreur de connexion à la base de données : " + e.getMessage());
                return null;
            }
        }
    }
}
