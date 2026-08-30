package com.saveur221.service;

import com.saveur221.config.PasswordUtil;
import com.saveur221.entities.Utilisateur;
import com.saveur221.enums.RoleType;
import com.saveur221.exceptions.EntityNotFoundException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.UtilisateurRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Contient les règles métier autour du personnel interne (ADMIN/GERANT).
 * Réservé à l'espace ADMIN (règle métier n°13 du sujet) : c'est la couche
 * View qui doit vérifier le rôle avant d'appeler ce service, mais on
 * revérifie ici aussi les règles indépendantes du rôle (email unique,
 * mot de passe >= 6 caractères...).
 */
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService() {
        this.utilisateurRepository = new UtilisateurRepository();
    }

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur ajouter(String nom, String prenom, String email, String motDePasseClair, RoleType role)
            throws SQLException, ValidationException {

        validerNomPrenom(nom, prenom);
        validerMotDePasse(motDePasseClair);

        if (utilisateurRepository.existeParEmail(email)) {
            throw new ValidationException("Cet email est déjà utilisé par un autre compte.");
        }

        String motDePasseHache = PasswordUtil.hash(motDePasseClair);
        Utilisateur utilisateur = new Utilisateur(0, nom.trim(), prenom.trim(), email.trim(),
                motDePasseHache, role, true);

        return utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> lister() throws SQLException {
        return utilisateurRepository.findAll();
    }

    public List<Utilisateur> rechercher(String motCle) throws SQLException {
        return utilisateurRepository.search(motCle);
    }

    public Utilisateur consulter(int id) throws SQLException, EntityNotFoundException {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur", id));
    }

    public void modifier(int id, String nom, String prenom, String email, RoleType role)
            throws SQLException, ValidationException, EntityNotFoundException {

        validerNomPrenom(nom, prenom);
        Utilisateur utilisateur = consulter(id);

        boolean emailChange = !utilisateur.getEmail().equalsIgnoreCase(email.trim());
        if (emailChange && utilisateurRepository.existeParEmail(email.trim())) {
            throw new ValidationException("Cet email est déjà utilisé par un autre compte.");
        }

        utilisateur.setNom(nom.trim());
        utilisateur.setPrenom(prenom.trim());
        utilisateur.setEmail(email.trim());
        utilisateur.setRole(role);

        utilisateurRepository.update(utilisateur);
    }

    public void changerMotDePasse(int id, String nouveauMotDePasseClair)
            throws SQLException, ValidationException, EntityNotFoundException {

        validerMotDePasse(nouveauMotDePasseClair);
        consulter(id); // vérifie qu'il existe
        utilisateurRepository.updateMotDePasse(id, PasswordUtil.hash(nouveauMotDePasseClair));
    }

    public void activer(int id) throws SQLException, EntityNotFoundException {
        consulter(id);
        utilisateurRepository.updateStatutActif(id, true);
    }

    /**
     * @param idConnecte l'id de l'utilisateur actuellement connecté, pour
     *                   empêcher un admin de désactiver son propre compte
     *                   et de se retrouver bloqué dehors.
     */
    public void desactiver(int id, int idConnecte)
            throws SQLException, ValidationException, EntityNotFoundException {

        if (id == idConnecte) {
            throw new ValidationException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        consulter(id);
        utilisateurRepository.updateStatutActif(id, false);
    }

    public void supprimer(int id, int idConnecte)
            throws SQLException, ValidationException, EntityNotFoundException {

        if (id == idConnecte) {
            throw new ValidationException("Vous ne pouvez pas supprimer votre propre compte.");
        }
        consulter(id);
        utilisateurRepository.delete(id);
    }

    private void validerNomPrenom(String nom, String prenom) throws ValidationException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new ValidationException("Le nom ne peut pas être vide.");
        }
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new ValidationException("Le prénom ne peut pas être vide.");
        }
    }

    private void validerMotDePasse(String motDePasse) throws ValidationException {
        if (motDePasse == null || motDePasse.length() < 6) {
            throw new ValidationException("Le mot de passe doit contenir au moins 6 caractères.");
        }
    }
}
