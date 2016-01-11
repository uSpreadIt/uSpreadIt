package it.uspread.core.service

import grails.transaction.Transactional
import it.uspread.core.domain.Role
import it.uspread.core.domain.User
import it.uspread.core.domain.UserRole

/**
 *  Service d'accés aux rôles de sécurité
 */
@Transactional
public class RoleService {

    /**
     * Applique le rôle {@link Role.ROLE_USER} a l'utilisateur
     * @param newModerator
     * @return
     */
    void setRoleUser(User newUser) {
        Role role = Role.where({ authority == Role.ROLE_USER }).find()
        UserRole usrRole = new UserRole(user: newUser, role: role)
        usrRole.save()
    }

    /**
     * Applique le rôle {@link Role.ROLE_MODERATOR} a l'utilisateur
     * @param newModerator
     * @return
     */
    void setRoleModerator(User newModerator) {
        Role role = Role.where({ authority == Role.ROLE_MODERATOR }).find()
        UserRole usrRole = new UserRole(user: newModerator, role: role)
        usrRole.save()
    }

    /**
     * Applique le rôle {@link Role.ROLE_ADMINISTRATOR} a l'utilisateur
     * @param newModerator
     * @return
     */
    void setRoleAdministrator(User newAdministrator) {
        Role role = Role.where({ authority == Role.ROLE_ADMINISTRATOR }).find()
        UserRole usrRole = new UserRole(user: newAdministrator, role: role)
        usrRole.save()
    }

    /**
     * Supprime les rôles de l'utilisateur
     * @param user
     * @return
     */
    void clearRole(User userToClear) {
        userToClear.specialUser = false
        UserRole.where({ user == userToClear }).each({ it.delete() })
    }
}