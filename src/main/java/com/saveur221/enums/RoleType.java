package com.saveur221.enums;

/**
 * Rôles des utilisateurs internes (personnel), utilisés pour l'authentification
 * dans le Module A (console). La table "roles" en base peut stocker d'autres
 * infos, mais côté logique métier ce sont les deux seules valeurs possibles
 * pour le personnel interne.
 */
public enum RoleType {
    ADMIN,
    GERANT
}
