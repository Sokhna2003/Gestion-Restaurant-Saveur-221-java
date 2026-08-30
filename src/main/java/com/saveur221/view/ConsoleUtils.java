package com.saveur221.view;

import java.util.Scanner;

/**
 * Petit utilitaire partagé par toutes les vues console pour lire les saisies
 * utilisateur et éviter de dupliquer un Scanner dans chaque classe.
 */
public class ConsoleUtils {

    private static final Scanner scanner = new Scanner(System.in);

    private ConsoleUtils() {
    }

    public static String lireTexte(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    /**
     * Redemande tant que l'utilisateur ne saisit rien : à utiliser pour
     * tout champ obligatoire (nom, libellé, etc.).
     */
    public static String lireTexteObligatoire(String message) {
        while (true) {
            String saisie = lireTexte(message);
            if (!saisie.isBlank()) {
                return saisie;
            }
            System.out.println("Ce champ est obligatoire.");
        }
    }

    public static int lireEntier(String message) {
        while (true) {
            System.out.print(message);
            String saisie = scanner.nextLine().trim();
            try {
                return Integer.parseInt(saisie);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez saisir un nombre valide.");
            }
        }
    }

    /**
     * Entier obligatoirement positif ou nul (quantités, seuils...).
     */
    public static int lireEntierPositifOuNul(String message) {
        while (true) {
            int valeur = lireEntier(message);
            if (valeur >= 0) {
                return valeur;
            }
            System.out.println("La valeur ne peut pas être négative.");
        }
    }

    public static double lireDouble(String message) {
        while (true) {
            System.out.print(message);
            String saisie = scanner.nextLine().trim();
            try {
                return Double.parseDouble(saisie);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez saisir un nombre valide.");
            }
        }
    }

    /**
     * Nombre décimal obligatoirement positif ou nul (prix...).
     */
    public static double lireDoublePositif(String message) {
        while (true) {
            double valeur = lireDouble(message);
            if (valeur >= 0) {
                return valeur;
            }
            System.out.println("La valeur ne peut pas être négative.");
        }
    }

    public static void pause() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

    public static void afficherTitre(String titre) {
        System.out.println("\n=== " + titre + " ===");
    }

    public static void afficherErreur(String message) {
        System.out.println("⚠ " + message);
    }
}
