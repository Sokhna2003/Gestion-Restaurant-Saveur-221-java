package com.saveur221.enums;

/**
 * Cycle de vie d'une commande (défini dans le cahier des charges) :
 * EN_ATTENTE -> EN_PREPARATION -> PRETE -> RETIREE
 * Une commande peut aussi être ANNULEE à tout moment avant RETIREE.
 */
public enum StatutCommande {
    EN_ATTENTE,
    EN_PREPARATION,
    PRETE,
    RETIREE,
    ANNULEE
}
