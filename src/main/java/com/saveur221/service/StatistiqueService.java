package com.saveur221.service;

import com.saveur221.entities.ProduitVendu;
import com.saveur221.enums.StatutCommande;
import com.saveur221.repository.StatistiqueRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

/**
 * Calcule les indicateurs du tableau de bord "Statistiques" du gérant :
 * chiffre d'affaires jour/semaine/mois, nombre de commandes, commandes en
 * cours, répartition par statut, produit le plus vendu et top 3.
 */
public class StatistiqueService {

    private final StatistiqueRepository statistiqueRepository;

    public StatistiqueService() {
        this.statistiqueRepository = new StatistiqueRepository();
    }

    public StatistiqueService(StatistiqueRepository statistiqueRepository) {
        this.statistiqueRepository = statistiqueRepository;
    }

    public BigDecimal caDuJour() throws SQLException {
        LocalDateTime debut = LocalDate.now().atStartOfDay();
        return statistiqueRepository.calculerChiffreAffaires(debut, LocalDateTime.now());
    }

    public BigDecimal caDeLaSemaine() throws SQLException {
        LocalDateTime debut = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        return statistiqueRepository.calculerChiffreAffaires(debut, LocalDateTime.now());
    }

    public BigDecimal caDuMois() throws SQLException {
        LocalDateTime debut = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();
        return statistiqueRepository.calculerChiffreAffaires(debut, LocalDateTime.now());
    }

    public int nombreCommandes() throws SQLException {
        return statistiqueRepository.compterCommandesTotal();
    }

    public int commandesEnCours() throws SQLException {
        return statistiqueRepository.compterCommandesEnCours();
    }

    public Map<StatutCommande, Integer> commandesParStatut() throws SQLException {
        return statistiqueRepository.compterCommandesParStatut();
    }

    /**
     * @return le produit le plus vendu, ou null si aucune vente enregistrée.
     */
    public ProduitVendu produitLePlusVendu() throws SQLException {
        List<ProduitVendu> top1 = statistiqueRepository.topProduitsVendus(1);
        return top1.isEmpty() ? null : top1.get(0);
    }

    public List<ProduitVendu> topTroisProduits() throws SQLException {
        return statistiqueRepository.topProduitsVendus(3);
    }
}
