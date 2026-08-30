package com.saveur221;

import com.saveur221.config.DatabaseConfig;
import com.saveur221.entities.Utilisateur;
import com.saveur221.view.LoginView;
import com.saveur221.view.MenuPrincipalView;

/**
 * Point d'entrée du Module A (application console).
 * Flux : connexion obligatoire -> menu principal routé selon le rôle.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Saveur 221 - Module A (Console) ===");

        Utilisateur utilisateur = new LoginView().afficher();

        if (utilisateur != null) {
            new MenuPrincipalView(utilisateur).afficher();
        } else {
            System.out.println("Fin du programme.");
        }

        DatabaseConfig.closeConnection();
    }
}
