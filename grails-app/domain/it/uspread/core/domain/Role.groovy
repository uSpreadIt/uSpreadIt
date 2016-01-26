package it.uspread.core.domain

/**
 * Sécurité : rôles disponibles
 */
class Role implements Serializable {

    private static final long serialVersionUID = 1

    /** Role d'un utilisateur */
    public static final String ROLE_PUBLIC = "ROLE_PUBLIC"
    /** Role d'un modérateur */
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR"
    /** Role d'un administrateur */
    public static final String ROLE_ADMINISTRATOR = "ROLE_ADMINISTRATOR"

    String authority

    static mapping = {
        version(false)
        cache(true)
        table('role')
    }

    static constraints = {
        authority(blank: false, unique: true)
    }

    @Override
    int hashCode() {
       authority?.hashCode() ?: 0
    }

    @Override
    boolean equals(Object other) {
       is(other) || (other instanceof Role && other.authority == authority)
    }

    @Override
    String toString() {
       authority
    }
}
