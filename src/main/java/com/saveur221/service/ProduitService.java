package src.main.java.com.saveur221.service;

import src.main.java.com.saveur221.entities.Categorie;
import src.main.java.com.saveur221.entities.Produit;
import src.main.java.com.saveur221.exceptions.EntityNotFoundException;
import src.main.java.com.saveur221.exceptions.ValidationException;
import src.main.java.com.saveur221.repository.CategorieRepository;
import src.main.java.com.saveur221.repository.ProduitRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Contient les règles métier autour des produits et du stock.
 * La couche View ne doit jamais appeler ProduitRepository directement.
 */
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;

    public ProduitService() {
        this.produitRepository = new ProduitRepository();
        this.categorieRepository = new CategorieRepository();
    }

    public ProduitService(ProduitRepository produitRepository, CategorieRepository categorieRepository) {
        this.produitRepository = produitRepository;
        this.categorieRepository = categorieRepository;
    }

    public Produit ajouter(String libelle, String description, BigDecimal prix,
                            int quantiteStock, int seuilAlerte, int categorieId, String image)
            throws SQLException, ValidationException, EntityNotFoundException {

        validerLibelle(libelle);
        validerPrix(prix);
        validerQuantite(quantiteStock);

        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie", categorieId));

        // Règle métier : si quantite_stock = 0, le produit doit être indisponible
        boolean disponible = quantiteStock > 0;

        Produit produit = new Produit(0, libelle.trim(), description, prix,
                quantiteStock, seuilAlerte, categorie, disponible, image);

        return produitRepository.save(produit);
    }

    public List<Produit> lister() throws SQLException {
        return produitRepository.findAll();
    }

    public Produit consulter(int id) throws SQLException, EntityNotFoundException {
        return produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit", id));
    }

    public List<Produit> rechercher(String motCle) throws SQLException {
        return produitRepository.search(motCle);
    }

    public List<Produit> filtrerParCategorie(int categorieId) throws SQLException {
        return produitRepository.findByCategorie(categorieId);
    }

    public List<Produit> listerDisponibles() throws SQLException {
        return produitRepository.findByDisponibilite(true);
    }

    public List<Produit> listerIndisponibles() throws SQLException {
        return produitRepository.findByDisponibilite(false);
    }

    public List<Produit> listerStockFaible() throws SQLException {
        return produitRepository.findStockFaible();
    }

    public List<Produit> listerEnRupture() throws SQLException {
        return produitRepository.findEnRupture();
    }

    public void modifier(int id, String libelle, String description, BigDecimal prix,
                          int seuilAlerte, int categorieId, boolean disponible, String image)
            throws SQLException, ValidationException, EntityNotFoundException {

        validerLibelle(libelle);
        validerPrix(prix);

        Produit produit = consulter(id);
        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie", categorieId));

        produit.setLibelle(libelle.trim());
        produit.setDescription(description);
        produit.setPrix(prix);
        produit.setSeuilAlerte(seuilAlerte);
        produit.setCategorie(categorie);
        // Un produit à 0 en stock reste indisponible même si on tente de le forcer
        produit.setDisponible(produit.getQuantiteStock() > 0 && disponible);
        produit.setImage(image);

        produitRepository.update(produit);
    }

    public void supprimer(int id) throws SQLException, EntityNotFoundException {
        consulter(id); // vérifie qu'il existe
        produitRepository.delete(id);
    }

    /**
     * Ajoute de la quantité au stock existant (réapprovisionnement).
     * Repasse automatiquement le produit en disponible.
     */
    public void approvisionner(int produitId, int quantiteAjoutee)
            throws SQLException, ValidationException, EntityNotFoundException {

        if (quantiteAjoutee <= 0) {
            throw new ValidationException("La quantité à approvisionner doit être positive.");
        }

        Produit produit = consulter(produitId);
        int nouvelleQuantite = produit.getQuantiteStock() + quantiteAjoutee;
        produitRepository.updateStock(produitId, nouvelleQuantite);
    }

    public void definirSeuilAlerte(int produitId, int seuil)
            throws SQLException, ValidationException, EntityNotFoundException {

        if (seuil < 0) {
            throw new ValidationException("Le seuil d'alerte ne peut pas être négatif.");
        }

        Produit produit = consulter(produitId);
        produit.setSeuilAlerte(seuil);
        produitRepository.update(produit);
    }

    private void validerLibelle(String libelle) throws ValidationException {
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new ValidationException("Le libellé du produit ne peut pas être vide.");
        }
    }

    private void validerPrix(BigDecimal prix) throws ValidationException {
        if (prix == null || prix.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Le prix doit être positif ou nul.");
        }
    }

    private void validerQuantite(int quantite) throws ValidationException {
        if (quantite < 0) {
            throw new ValidationException("La quantité en stock ne peut pas être négative.");
        }
    }
}
