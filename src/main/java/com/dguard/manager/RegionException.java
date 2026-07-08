package com.dguard.manager;

/**
 * Erreur "attendue" (mauvais usage, région introuvable, sélection manquante...) destinée
 * à être affichée telle quelle à l'utilisateur par la commande qui l'a déclenchée.
 */
public class RegionException extends RuntimeException {

    public RegionException(String message) {
        super(message);
    }
}
