package com.saveur221.service;

import com.saveur221.entities.Commande;
import com.saveur221.entities.Paiement;
import com.saveur221.enums.StatutCommande;
import com.saveur221.exceptions.EntityNotFoundException;
import com.saveur221.exceptions.ValidationException;
import com.saveur221.repository.CommandeRepository;
import com.saveur221.repository.PaiementRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Contient la règle métier centrale des paiements : le total des paiements
 * d'une commande ne doit jamais dépasser son montant (règle n°12 du sujet).
 */
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;

    public PaiementService() {
        this.paiementRepository = new PaiementRepository();
        this.commandeRepository = new CommandeRepository();
    }

    public PaiementService(PaiementRepository paiementRepository, CommandeRepository commandeRepository) {
        this.paiementRepository = paiementRepository;
        this.commandeRepository = commandeRepository;
    }

    public List<Commande> listerImpayees() throws SQLException {
        return paiementRepository.findCommandesImpayees();
    }

    public List<Commande> listerPartiellementPayees() throws SQLException {
        return paiementRepository.findCommandesPartiellementPayees();
    }

    public List<Paiement> listerParCommande(int commandeId) throws SQLException {
        return paiementRepository.findByCommande(commandeId);
    }

    public BigDecimal montantRestant(int commandeId) throws SQLException, EntityNotFoundException {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande", commandeId));
        BigDecimal dejaPaye = paiementRepository.montantDejaPaye(commandeId);
        return commande.getMontantTotal().subtract(dejaPaye);
    }

    /**
     * Enregistre un paiement pour une commande, en vérifiant que le total
     * des paiements ne dépasse jamais le montant de la commande.
     */
    public Paiement enregistrer(int commandeId, BigDecimal montant, String modePaiement)
            throws SQLException, ValidationException, EntityNotFoundException {

        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Le montant du paiement doit être positif.");
        }
        if (modePaiement == null || modePaiement.isBlank()) {
            throw new ValidationException("Le mode de paiement est obligatoire.");
        }

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande", commandeId));

        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new ValidationException("Impossible d'enregistrer un paiement pour une commande annulée.");
        }

        BigDecimal dejaPaye = paiementRepository.montantDejaPaye(commandeId);
        BigDecimal restant = commande.getMontantTotal().subtract(dejaPaye);

        if (montant.compareTo(restant) > 0) {
            throw new ValidationException(
                    "Le montant dépasse le solde restant (" + restant + "). Paiement refusé.");
        }

        Paiement paiement = new Paiement(0, commande, montant, null, modePaiement.trim());
        return paiementRepository.save(paiement);
    }
}
