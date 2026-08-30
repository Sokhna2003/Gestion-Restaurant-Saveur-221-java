package com.saveur221.view;

import com.saveur221.entities.Utilisateur;
import com.saveur221.enums.RoleType;

import java.util.List;

/**
 * Vue pour la gestion des utilisateurs internes (espace ADMIN uniquement) :
 * affichage et saisie uniquement. Ne connaît pas UtilisateurService — seul
 * MenuPrincipalView a le droit d'appeler la couche Service.
 */
public class UtilisateurView {

    public int demanderChoix() {
        ConsoleUtils.afficherTitre("Gestion des utilisateurs (ADMIN)");
        System.out.println("1. Ajouter un utilisateur");
        System.out.println("2. Afficher tous les utilisateurs");
        System.out.println("3. Rechercher un utilisateur");
        System.out.println("4. Modifier un utilisateur");
        System.out.println("5. Changer le mot de passe d'un utilisateur");
        System.out.println("6. Activer un compte");
        System.out.println("7. Désactiver un compte");
        System.out.println("8. Supprimer un utilisateur");
        System.out.println("0. Retour au menu principal");
        return ConsoleUtils.lireEntier("Votre choix : ");
    }

    public Utilisateur demanderNouvelUtilisateur() {
        String nom = ConsoleUtils.lireTexteObligatoire("Nom (obligatoire) : ");
        String prenom = ConsoleUtils.lireTexteObligatoire("Prénom (obligatoire) : ");
        String email = ConsoleUtils.lireTexteObligatoire("Email (obligatoire) : ");
        String motDePasse = demanderMotDePasseValide("Mot de passe (6 caractères min, obligatoire) : ");
        RoleType role = demanderRole();

        // Le mot de passe en clair est transporté dans l'objet le temps
        // que MenuPrincipalView le passe au Service, qui le hachera avant
        // stockage : cet objet Utilisateur "brouillon" n'est jamais persisté tel quel.
        return new Utilisateur(0, nom, prenom, email, motDePasse, role, true);
    }

    public RoleType demanderRole() {
        System.out.println("Rôle : 1. ADMIN   2. GERANT");
        int choix = ConsoleUtils.lireEntier("Choix : ");
        while (choix != 1 && choix != 2) {
            System.out.println("Choix invalide.");
            choix = ConsoleUtils.lireEntier("Choix : ");
        }
        return choix == 1 ? RoleType.ADMIN : RoleType.GERANT;
    }

    public Utilisateur demanderModification(Utilisateur existant) {
        System.out.println("(Laisser vide pour garder la valeur actuelle)");

        String nom = ConsoleUtils.lireTexte("Nom [" + existant.getNom() + "] : ");
        if (nom.isBlank()) nom = existant.getNom();

        String prenom = ConsoleUtils.lireTexte("Prénom [" + existant.getPrenom() + "] : ");
        if (prenom.isBlank()) prenom = existant.getPrenom();

        String email = ConsoleUtils.lireTexte("Email [" + existant.getEmail() + "] : ");
        if (email.isBlank()) email = existant.getEmail();

        System.out.println("Changer le rôle actuel (" + existant.getRole() + ") ? (o/N)");
        RoleType role = existant.getRole();
        if (ConsoleUtils.lireTexte("> ").equalsIgnoreCase("o")) {
            role = demanderRole();
        }

        return new Utilisateur(existant.getId(), nom, prenom, email,
                existant.getMotDePasse(), role, existant.isActif());
    }

    public int demanderId(String action) {
        return ConsoleUtils.lireEntier("Id de l'utilisateur à " + action + " : ");
    }

    public String demanderMotCle() {
        return ConsoleUtils.lireTexte("Nom, prénom ou email à rechercher : ");
    }

    public String demanderNouveauMotDePasse() {
        return demanderMotDePasseValide("Nouveau mot de passe (6 caractères min) : ");
    }

    private String demanderMotDePasseValide(String message) {
        while (true) {
            String saisie = ConsoleUtils.lireTexte(message);
            if (saisie.length() >= 6) {
                return saisie;
            }
            System.out.println("Le mot de passe doit contenir au moins 6 caractères.");
        }
    }

    public boolean confirmerSuppression() {
        return ConsoleUtils.lireTexte("Confirmer la suppression ? (o/N) : ").equalsIgnoreCase("o");
    }

    public void afficherUtilisateurs(List<Utilisateur> utilisateurs) {
        if (utilisateurs.isEmpty()) {
            System.out.println("Aucun utilisateur à afficher.");
            return;
        }
        for (Utilisateur u : utilisateurs) {
            System.out.printf("[%d] %s %s <%s> - %s - %s%n",
                    u.getId(), u.getPrenom(), u.getNom(), u.getEmail(), u.getRole(),
                    u.isActif() ? "actif" : "désactivé");
        }
    }

    public void afficherMessage(String message) {
        System.out.println(message);
    }
}
