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
        }.each { ((Message) it).removeFromReceivedBy(new Spread(user)) }

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
                eq('user.id', user.id)
            }
        }.each { ((Message) it).removeFromSpreadBy(new Spread(user)) }

        Spread.createCriteria().list {
            eq('user.id', user.id)
        }.each{ ((Spread) it).delete(flush: true) }

        user.delete(flush: true)
    }

    public void addAndroidPushToken(User user, String androidPushToken) {
        if (!user.androidPushTokens.contains(androidPushToken)) {
            user.androidPushTokens.add(androidPushToken)
        }
    }

    public List<User> getTopUsers() {
        return User.list([max: TOP_SIZE, sort: 'score', order: 'desc'])
    }
}
