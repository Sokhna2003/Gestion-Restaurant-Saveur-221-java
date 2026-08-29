package src.main.java.com.saveur221.service;

import src.main.java.com.saveur221.entities.Categorie;
import src.main.java.com.saveur221.exceptions.EntityNotFoundException;
import src.main.java.com.saveur221.exceptions.ValidationException;
import src.main.java.com.saveur221.repository.CategorieRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Contient les règles métier autour des catégories.
 * La couche View ne doit jamais appeler CategorieRepository directement :
 * elle passe toujours par ce Service (contrainte du prof).
 */
public class CategorieService {

    private final CategorieRepository categorieRepository;

    public CategorieService() {
        this.categorieRepository = new CategorieRepository();
    }

    // Permet d'injecter un repository (utile pour les tests unitaires avec un mock)
    public CategorieService(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    public Categorie ajouter(String nom, String description) throws SQLException, ValidationException {
        validerNom(nom);
        Categorie categorie = new Categorie(0, nom.trim(), description);
        return categorieRepository.save(categorie);
    }

    public List<Categorie> lister() throws SQLException {
        return categorieRepository.findAll();
    }

    public List<Categorie> rechercher(String motCle) throws SQLException {
        return categorieRepository.search(motCle);
    }

    public Categorie consulter(int id) throws SQLException, EntityNotFoundException {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie", id));
    }

    public void modifier(int id, String nom, String description)
            throws SQLException, ValidationException, EntityNotFoundException {
        validerNom(nom);
        Categorie categorie = consulter(id); // vérifie qu'elle existe
        categorie.setNom(nom.trim());
        categorie.setDescription(description);
        categorieRepository.update(categorie);
    }

    /**
     * Règle métier n°9 du sujet : une catégorie contenant des produits
     * ne peut pas être supprimée. On vérifie explicitement avant de
     * supprimer pour donner un message clair, plutôt que de compter
     * uniquement sur l'exception SQL de la contrainte ON DELETE RESTRICT.
     */
    public void supprimer(int id) throws SQLException, ValidationException, EntityNotFoundException {
        consulter(id); // vérifie qu'elle existe

        if (categorieRepository.estUtiliseeParDesProduits(id)) {
            throw new ValidationException(
                    "Impossible de supprimer cette catégorie : elle contient encore des produits.");
        }

        try {
            categorieRepository.delete(id);
        } catch (SQLException e) {
            // Filet de sécurité si la vérification ci-dessus a été contournée
            // (ex: produit ajouté entre-temps par un autre utilisateur)
            throw new ValidationException(
                    "Impossible de supprimer cette catégorie : elle est encore utilisée.");
        }
    }

    private void validerNom(String nom) throws ValidationException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new ValidationException("Le nom de la catégorie ne peut pas être vide.");
        }
    }
}
