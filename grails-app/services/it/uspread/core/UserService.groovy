package it.uspread.core

import grails.transaction.Transactional

@Transactional
class UserService {

    // TODO à paramétrer
    private static final int TOP_SIZE = 50;

    public User getUserFromId(Long userId) {
        return User.where { id == userId }.find()
    }

    public void deleteUser(User user) {
        Message.createCriteria().list {
            receivedBy {
                eq('user.id', user.id)
            }
        }.each { ((Message) it).removeFromReceivedBy(((Message)it).receivedBy.find { r -> r.user == user}) } // TODO peut etre réécrire la requete suite a mon edit rapid pour faire marcher avec nouveau modèle

        Message.createCriteria().list {
            ignoredBy {
                eq('id', user.id)
            }
        }.each { ((Message) it).removeFromIgnoredBy(user) }

        Message.createCriteria().list {
            reports {
                eq('reporter.id', user.id)
            }
        }.each {
            ((Message)it).removeFromReports(new Report(user)) // FIXME ça fonctionne ça ?? si c'est le cas ça m'arangerai bien pour régler les TODO autour de la meme manière
        }

        Report.createCriteria().list {
            reporter {
                eq('id', user.id)
            }
        }.each{ ((Report) it).delete(flush: true) }

        Message.createCriteria().list {
            spreadBy {
                eq('user.id', user.id)
            }
        }.each { ((Message) it).removeFromSpreadBy(((Message)it).spreadBy.find { s -> s.user == user}) }

        user.delete(flush: true)
    }

    public List<User> getTopUsers() {
        return User.list([max: TOP_SIZE, sort: 'score', order: 'desc'])
    }
}
