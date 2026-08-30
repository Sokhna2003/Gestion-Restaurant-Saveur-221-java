package com.saveur221.exceptions;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String entite, int id) {
        super(entite + " introuvable avec l'id " + id);
    }
}
