package src.main.java.com.saveur221.service;

import src.main.java.com.saveur221.config.PasswordUtil;
import src.main.java.com.saveur221.entities.Utilisateur;
import src.main.java.com.saveur221.exceptions.AuthentificationException;
import src.main.java.com.saveur221.repository.UtilisateurRepository;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Gère la connexion du personnel interne (Module A).
 * Règle métier n°1 du sujet : un utilisateur désactivé ne peut pas se connecter.
 * Le système doit vérifier, dans l'ordre : existence, mot de passe, compte actif.
 */
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;

    public AuthService() {
        this.utilisateurRepository = new UtilisateurRepository();
    }

    public AuthService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Tente de connecter un utilisateur. Ne précise pas si c'est l'email ou
     * le mot de passe qui est faux (bonne pratique de sécurité), sauf pour
     * le cas "compte désactivé" qui est un message dédié.
     */
    public Utilisateur login(String email, String motDePasse) throws SQLException, AuthentificationException {
        Optional<Utilisateur> resultat = utilisateurRepository.findByEmail(email);

        if (resultat.isEmpty()) {
            throw new AuthentificationException("Email ou mot de passe incorrect.");
        }

        Utilisateur utilisateur = resultat.get();

        if (!PasswordUtil.verifier(motDePasse, utilisateur.getMotDePasse())) {
            throw new AuthentificationException("Email ou mot de passe incorrect.");
        }

        if (!utilisateur.isActif()) {
            throw new AuthentificationException("Ce compte a été désactivé. Contactez l'administrateur.");
        }

        return utilisateur;
    }
}
