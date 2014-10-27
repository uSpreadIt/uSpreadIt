package it.uspread.core

class Role {

    public static final String ROLE_MODERATOR = "ROLE_MODERATOR"

    String authority

    static mapping = { cache true }

    static constraints = {
        authority blank: false, unique: true
    }
}
