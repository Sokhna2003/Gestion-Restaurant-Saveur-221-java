package com.saveur221.service;

import com.saveur221.entities.Categorie;
import com.saveur221.exceptions.EntityNotFoundException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.CategorieRepository;

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
     * Déplace une catégorie dans la corbeille (soft delete) plutôt qu'une
     * suppression physique, pour rester cohérent avec le Module B (PHP).
     * Règle métier (partagée avec le Module B) : une catégorie contenant
     * des produits actifs ne peut pas être supprimée.
     */
    public void supprimer(int id) throws SQLException, ValidationException, EntityNotFoundException {
        consulter(id); // vérifie qu'elle existe et est active

        int nbProduits = categorieRepository.compterProduits(id, false);
        if (nbProduits > 0) {
            throw new ValidationException(
                    "Impossible de supprimer cette catégorie : elle contient " + nbProduits + " produit(s).");
        }

        categorieRepository.delete(id);
    }

    public List<Categorie> listerCorbeille() throws SQLException {
        return categorieRepository.findAllSupprimees();
    }

    /**
     * Restaure une catégorie supprimée (sort de la corbeille, supprime_le = NULL).
     */
    public void restaurer(int id) throws SQLException, ValidationException, EntityNotFoundException {
        Categorie categorie = categorieRepository.findByIdIncluantSupprimee(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie", id));
        if (!categorie.isSupprimee()) {
            throw new ValidationException("Cette catégorie n'est pas dans la corbeille.");
        }
        categorieRepository.restaurer(id);
    }

    /**
     * Suppression physique (définitive). Nécessite une catégorie en corbeille
     * et sans produit lié, même en corbeille (sinon violation de la clé
     * étrangère) — même règle que le Module B (PHP).
     */
    public void supprimerDefinitivement(int id)
            throws SQLException, ValidationException, EntityNotFoundException {

        Categorie categorie = categorieRepository.findByIdIncluantSupprimee(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie", id));
        if (!categorie.isSupprimee()) {
            throw new ValidationException("Cette catégorie n'est pas dans la corbeille.");
        }

        int nbProduits = categorieRepository.compterProduits(id, true);
        if (nbProduits > 0) {
            throw new ValidationException(
                    "Suppression impossible : " + nbProduits + " produit(s) sont encore liés à cette catégorie.");
        }

        categorieRepository.supprimerDefinitivement(id);
    }

    private void validerNom(String nom) throws ValidationException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new ValidationException("Le nom de la catégorie ne peut pas être vide.");
        }
    }
}
