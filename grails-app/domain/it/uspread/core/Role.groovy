package it.uspread.core

/**
 * Sécurité : rôles spéciaux disponibles (Pas de rôle USER car ne pas avoir de rôles est la condition pour être un simple utilisateur)
 */
class Role {

    /** Role d'un modérateur */
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR"
    /** Role d'un administrateur */
    public static final String ROLE_ADMINISTRATOR = "ROLE_ADMINISTRATOR"

    String authority

    static mapping = {
        version(false)
        cache(true)
    }

    static constraints = {
        authority(blank: false, unique: true)
    }
}
