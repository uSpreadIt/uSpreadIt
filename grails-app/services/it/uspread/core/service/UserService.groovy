package it.uspread.core.service

import it.uspread.core.domain.Message;
import it.uspread.core.domain.User;
import grails.transaction.Transactional

/**
 * Service utilisateur
 */
@Transactional
class UserService {

    def messageService
    def roleService

    // TODO à paramétrer
    private static final int TOP_SIZE = 50;

    /**
     *
     * @param userId
     * @return
     */
    User getUserFromId(Long userId) {
        return User.get(userId)
    }

    /**
     * Suppression d'un utilisateur, de ses messages,
     * et de ses propres informations de propagation, reception, ignore et signalement de message d'autres utilisateurs
     * @param user Un utilisateur
     */
    void deleteUser(User user) {
        // TODO l'ensemble de cette méthode devra probablement être faite en asyncrhrone
        // TODO optimisation de ces 4 permiers blocs à travailler sans nul doute
        Message.createCriteria().list {
            receivedBy { eq('user.id', user.id) }
        }.each { Message m ->
            m.removeFromReceivedBy(m.getReceivedFor(user))
            m.save([flush: true])
        }

        Message.createCriteria().list {
            spreadBy { eq('user.id', user.id) }
        }.each { Message m ->
            m.removeFromSpreadBy(m.getSpreadFor(user))
            m.save([flush: true])
        }

        Message.createCriteria().list {
            ignoredBy { eq('id', user.id) }
        }.each { Message m ->
            m.removeFromIgnoredBy(user)
            m.save([flush: true])
        }

        Message.createCriteria().list {
            reports({ eq('reporter.id', user.id) })
        }.each { Message m ->
            m.removeFromReports(m.getReportBy(user))
            m.save([flush: true])
        }

        // Si l'utilisateur avait des message on notifie qu'ils vont être supprimé
        if (user.messages) {
            user.messages.each({ Message m ->
                messageService.notifyMessageWillDelete(m)
            })
        }

        // Si l'utilisateur est un specialuser il faut virer ses droits
        if (user.isSpecialUser()) {
            roleService.clearRole(user)
        }

        user.delete([flush: true])
    }

    /**
     * TODO conception
     * @return
     */
    List<User> getTopUsers() {
        return User.list([max: TOP_SIZE, sort: 'score', order: 'desc'])
    }
}
