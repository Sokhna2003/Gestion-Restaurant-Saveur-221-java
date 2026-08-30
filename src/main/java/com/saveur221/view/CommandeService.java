package com.saveur221.service;

import com.saveur221.entities.Commande;
import com.saveur221.entities.LigneCommande;
import com.saveur221.enums.StatutCommande;
import com.saveur221.exceptions.EntityNotFoundException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.CommandeRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Contient les règles métier autour des commandes, côté personnel interne
 * (Module A). La création de commandes est réservée au client via le
 * Module B (PHP) : ce service se limite à consulter, filtrer, changer le
 * statut et annuler (avec restitution du stock, règle métier n°8).
 */
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ProduitService produitService;

    public CommandeService() {
        this.commandeRepository = new CommandeRepository();
        this.produitService = new ProduitService();
    }

    public CommandeService(CommandeRepository commandeRepository, ProduitService produitService) {
        this.commandeRepository = commandeRepository;
        this.produitService = produitService;
    }

    public List<Commande> lister() throws SQLException {
        return commandeRepository.findAll();
    }

    public List<Commande> filtrerParStatut(StatutCommande statut) throws SQLException {
        return commandeRepository.findByStatut(statut);
    }

    public List<Commande> rechercher(String motCle) throws SQLException {
        return commandeRepository.search(motCle);
    }

    /**
     * Charge une commande avec le détail complet de ses lignes.
     */
    public Commande consulter(int id) throws SQLException, EntityNotFoundException {
        return commandeRepository.findByIdAvecLignes(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande", id));
    }

    /**
     * Fait avancer une commande d'un seul cran dans le cycle
     * EN_ATTENTE -> EN_PREPARATION -> PRETE -> RETIREE.
     * Pour annuler une commande, utiliser annuler() plutôt que ce
     * changerStatut(ANNULEE) : l'annulation a un traitement dédié
     * (restitution du stock).
     */
    public void changerStatut(int commandeId, StatutCommande nouveauStatut)
            throws SQLException, EntityNotFoundException, ValidationException {

        if (nouveauStatut == StatutCommande.ANNULEE) {
            throw new ValidationException("Pour annuler une commande, utilise l'option \"Annuler\" dédiée.");
        }

        Commande commande = consulter(commandeId);
        StatutCommande statutActuel = commande.getStatut();

        if (statutActuel == StatutCommande.RETIREE || statutActuel == StatutCommande.ANNULEE) {
            throw new ValidationException("Cette commande est déjà terminée (" + statutActuel + "), son statut ne peut plus changer.");
        }

        boolean estLeStatutSuivant = nouveauStatut.ordinal() == statutActuel.ordinal() + 1;
        if (!estLeStatutSuivant) {
            throw new ValidationException(
                    "Transition invalide : une commande " + statutActuel + " ne peut passer qu'à l'étape suivante.");
        }

        commandeRepository.updateStatut(commandeId, nouveauStatut);
    }

    /**
     * Annule une commande et restitue le stock de chaque produit commandé
     * (règle métier n°8 du sujet).
     */
    public void annuler(int commandeId)
            throws SQLException, EntityNotFoundException, ValidationException {

        Commande commande = consulter(commandeId); // charge aussi les lignes

        if (commande.getStatut() == StatutCommande.RETIREE) {
            throw new ValidationException("Impossible d'annuler une commande déjà retirée.");
        }
        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new ValidationException("Cette commande est déjà annulée.");
        }

        for (LigneCommande ligne : commande.getLignes()) {
            try {
                produitService.approvisionner(ligne.getProduit().getId(), ligne.getQuantite());
            } catch (EntityNotFoundException | ValidationException e) {
                // Le produit a pu être supprimé entre-temps : on log et on continue
                // plutôt que de bloquer toute l'annulation.
                System.err.println("Impossible de restituer le stock du produit "
                        + ligne.getProduit().getId() + " : " + e.getMessage());
            }
        }

        commandeRepository.updateStatut(commandeId, StatutCommande.ANNULEE);
    }
}