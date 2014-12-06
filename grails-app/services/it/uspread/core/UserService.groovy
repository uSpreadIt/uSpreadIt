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
            sentTo {
                eq('id', user.id)
            }
        }.each { ((Message) it).removeFromSentTo(user) }

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
            ((Message)it).removeFromReports(new Report(user))
        }

        Report.createCriteria().list {
            reporter {
                eq('id', user.id)
            }
        }.each{ ((Report) it).delete(flush: true) }

        Message.createCriteria().list {
            spreadBy {
                eq('id', user.id)
            }
        }.each { ((Message) it).removeFromSpreadBy(user) }

        user.delete(flush: true)
    }

    public List<User> getTopUsers() {
        return User.list([max: TOP_SIZE, sort: 'score', order: 'desc'])
    }
}
