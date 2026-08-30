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
