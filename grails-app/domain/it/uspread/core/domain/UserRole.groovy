package it.uspread.core.domain

import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Sécurité : association entre les utilisateurs et les rôles.
 * Un utilisateur associé à aucun rôle est considéré comme étant un simple utilisateur car il n'aura jamais d'autres roles
 */
class UserRole implements Serializable {

    private static final long serialVersionUID = 1

    /** Utilisateur */
    User user
    /** Role associé */
    Role role

    static mapping = {
        id(composite: ['role', 'user'])
        version(false)
    }

    static constraints = {
        role(validator: { Role r, UserRole ur ->
            if (ur.user == null){
                return
            }
            boolean existing = false
            UserRole.withNewSession {
                existing = UserRole.exists(ur.user.id, r.id)
            }
            if (existing) {
                return 'userRole.exists'
            }
        })
    }

    @Override
    boolean equals(Object other) {
        if (is(other)) {
            return true
        }
        if (!(other instanceof UserRole)) {
            return false
        }

        return other.user?.id == user?.id && other.role?.id == role?.id
    }

    @Override
    int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder()
        builder.append(user.id)
        builder.append(role.id)
        return builder.toHashCode()
    }

    static UserRole get(long userId, long roleId) {
        UserRole.where {
            user == User.load(userId) && role == Role.load(roleId)
        }.get()
    }

    static boolean exists(long userId, long roleId) {
        UserRole.where {
            user == User.load(userId) && role == Role.load(roleId)
        }.count() > 0
    }

    static UserRole create(User user, Role role, boolean flush = false) {
        def instance = new UserRole(user: user, role: role)
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean remove(User u, Role r, boolean flush = false) {
        if (u == null || r == null) {
            return false
        }

        int rowCount = UserRole.where {
            user == User.load(u.id) && role == Role.load(r.id)
        }.deleteAll()

        if (flush) {
            UserRole.withSession { it.flush() } }

        rowCount > 0
    }

    static void removeAll(User u, boolean flush = false) {
        if (u == null){
            return
        }

        UserRole.where {
            user == User.load(u.id)
        }.deleteAll()

        if (flush) {
            UserRole.withSession { it.flush() } }
    }

    static void removeAll(Role r, boolean flush = false) {
        if (r == null){
            return
        }

        UserRole.where {
            role == Role.load(r.id)
        }.deleteAll()

        if (flush) {
            UserRole.withSession { it.flush() } }
    }
}
