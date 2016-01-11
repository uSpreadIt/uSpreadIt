package it.uspread.core.service

import grails.transaction.Transactional
import it.uspread.core.data.Status
import it.uspread.core.domain.Message
import it.uspread.core.domain.Report
import it.uspread.core.domain.Spread
import it.uspread.core.domain.User
import it.uspread.core.params.MessageCriteria
import it.uspread.core.params.URLParamsValue
import it.uspread.core.type.ReportType

/**
 * Service des messages
 */
@Transactional
class MessageService {

    def iosAPNSService
    def androidGcmService

    // TODO à paramétrer
    private static final int MAX_MESSAGES_PER_DAY = 1000
    private static final int SPREAD_SIZE = 10

    /**
     * Recherche les messages écrits par un utilisateur
     * @param id Id de l'auteur des messages
     * @param msgCriteria Critères de recherche supplémentaires
     * @return liste de messages
     */
    List<Message> getMessagesWritedByAuthorId(Long id, MessageCriteria msgCriteria) {
        def listMap = [sort: 'dateCreated', order: 'desc']

        if (msgCriteria != null && msgCriteria.getCount() > 0) {
            listMap['max'] = msgCriteria.getCount()
        }

        List<Message> listMessage;
        // Recherche des messages avec critère de date
        if (msgCriteria?.getOperator() != null) {
            if (URLParamsValue.OPERATOR_GREATER == msgCriteria.getOperator()) {
                listMessage = Message.where({ author.id == id && dateCreated > msgCriteria.getDate() }).list(listMap)
            } else if (URLParamsValue.OPERATOR_GREATER_OR_EQUALS == msgCriteria.getOperator()) {
                listMessage = Message.where({ author.id == id && dateCreated >= msgCriteria.getDate() }).list(listMap)
            } else {
                listMessage = Message.where({ author.id == id && dateCreated < msgCriteria.getDate() }).list(listMap)
            }
        }
        // Recherche des messages sans critère de date
        else {
            listMessage = Message.where({ author.id == id }).list(listMap)
        }
        return listMessage
    }

    /**
     * Recherche les messages reçus par un utilisateur
     * @param id Id de l'utilisateur recevant les messages
     * @param msgCriteria Critères de recherche supplémentaires
     * @return liste de messages
     */
    List<Message> getMessagesReceivedByUserId(Long id, MessageCriteria msgCriteria) {
        def listMap = [:]
        if (msgCriteria != null && msgCriteria.getCount() > 0) {
            listMap['max'] = msgCriteria.getCount()
        }

        List<Message> listMessage;
        // Recherche des messages avec critère de date
        if (msgCriteria?.getOperator() != null) {
            if (URLParamsValue.OPERATOR_GREATER == msgCriteria.getOperator()) {
                listMessage =  Message.createCriteria().list(listMap, {
                    receivedBy {
                        eq('user.id', id)
                        gt('date', msgCriteria.getDate())
                        order('date', 'desc')
                    }
                })
            } else if (URLParamsValue.OPERATOR_GREATER_OR_EQUALS == msgCriteria.getOperator()) {
                listMessage =  Message.createCriteria().list(listMap, {
                    receivedBy {
                        eq('user.id', id)
                        ge('date', msgCriteria.getDate())
                        order('date', 'desc')
                    }
                })
            } else {
                listMessage =  Message.createCriteria().list(listMap, {
                    receivedBy {
                        eq('user.id', id)
                        lt('date', msgCriteria.getDate())
                        order('date', 'desc')
                    }
                })
            }
        }
        // Recherche des messages sans critère de date
        else {
            listMessage =  Message.createCriteria().list(listMap, {
                receivedBy {
                    eq('user.id', id)
                    order('date', 'desc')
                }
            })
        }
        return listMessage
    }

    /**
     * Recherche les messages propagés par un utilisateur
     * @param id Id de l'utilisateur propagant les messages
     * @param msgCriteria Critères de recherche supplémentaires
     * @return liste de messages
     */
    List<Message> getMessagesSpreadByUserId(Long id, MessageCriteria msgCriteria) {
        def listMap = [:]
        if (msgCriteria != null && msgCriteria.getCount() > 0) {
            listMap['max'] = msgCriteria.getCount()
        }

        List<Message> listMessage
        // Recherche des messages avec critère de date
        if (msgCriteria?.getOperator() != null) {
            if (URLParamsValue.OPERATOR_GREATER == msgCriteria.getOperator()) {
                listMessage =  Message.createCriteria().list(listMap, {
                    spreadBy {
                        eq('user.id', id)
                        gt('date', msgCriteria.getDate())
                        order('date', 'desc')
                    }
                })
            } else if (URLParamsValue.OPERATOR_GREATER_OR_EQUALS == msgCriteria.getOperator()) {
                listMessage =  Message.createCriteria().list(listMap, {
                    spreadBy {
                        eq('user.id', id)
                        ge('date', msgCriteria.getDate())
                        order('date', 'desc')
                    }
                })
            } else {
                listMessage =  Message.createCriteria().list(listMap, {
                    spreadBy {
                        eq('user.id', id)
                        lt('date', msgCriteria.getDate())
                        order('date', 'desc')
                    }
                })
            }
        }
        // Recherche des messages sans critère de date
        else {
            listMessage =  Message.createCriteria().list(listMap, {
                spreadBy {
                    eq('user.id', id)
                    order('date', 'desc')
                }
            })
        }
        return listMessage
    }

    /**
     * Recherche des messages signalés
     * @param user de l'utilisateur concerné ou null
     * @return liste de messages
     */
    List<Message> getReportedMessages(User user) {
        if (user) {
            return (List<Message>) Message.createCriteria().list({
                eq('author.id', user.id)
                and(reports({ isNotNull('id') }))
            })
        } else {
            return (List<Message>) Message.createCriteria().list({ reports({ isNotNull('id') }) })
        }
    }

    /**
     * Retourne les informations de status de l'utilisateur vis à vis des messages
     * @param user
     * @return
     */
    Status getUserMessagesStatus(User user, boolean quotaOnly) {
        Status status = new Status()
        status.setQuotaReached(isMessageCreationLimitReached(user))
        if (!quotaOnly) {
            status.setNbMessageWrited(Message.createCriteria().count({ eq('author.id', user.id) }))
            status.setNbMessageSpread(Message.createCriteria().count({ spreadBy{ eq('user.id', user.id) } }))
            status.setNbMessageIgnored(Message.createCriteria().count({ ignoredBy{ eq('id', user.id) } }))
            status.setNbMessageReported(Message.createCriteria().count({ reports{ eq('reporter.id', user.id) } }))
        }
        return status
    }

    /**
     * Indique si le quota de nouveau message de l'utilisateur est atteint.<br>
     * Sur les dernières 24 heures on limite à 2 messages max.
     * TODO Cette vérification devra être géré de façon atomique (possible en prenant en compte la scalabilité des serveurs ?)
     * @return true si quota atteint
     */
    boolean isMessageCreationLimitReached(User user) {
        def startDate = (new Date()).minus(1);
        def nbMessage = Message.where({ author.id == user.id && dateCreated > startDate }).count()
        return nbMessage >= MAX_MESSAGES_PER_DAY
    }

    /**
     * Propagation d'un message
     * @param message le message à propager
     * @param initialSpread pour distinguer la création d'un nouveau message de la propagation
     */
    void spreadIt(Message message, boolean initialSpread) {
        // Select spreadSize users order by lastReceivedMessageDate asc
        List<User> recipients
        if (initialSpread) {
            recipients = User.findAllBySpecialUserAndIdNotEqual(
                    false, message.authorId, [max: SPREAD_SIZE, sort: 'lastReceivedMessageDate', order: 'asc'])
        } else {
            // Un message signalé est aussi ignoré donc pas nécessaire d'utiliser la collection des éléments reporté
            def usersWhoReceivedThisMessage = message.ignoredBy.collect({ it.id })
            usersWhoReceivedThisMessage.addAll(message.receivedBy.collect({ it.user.id }))
            usersWhoReceivedThisMessage.addAll(message.spreadBy.collect({ it.user.id }))

            recipients = User.findAllBySpecialUserAndIdNotEqualAndIdNotInList(
                    false, message.authorId, usersWhoReceivedThisMessage, [max: SPREAD_SIZE, sort: 'lastReceivedMessageDate', order: 'asc'])
        }
        recipients = recipients.size() >= SPREAD_SIZE ? recipients[0..SPREAD_SIZE - 1] : recipients
        Date now = new Date()
        recipients.each {
            it.lastReceivedMessageDate = now
            message.addToReceivedBy(new Spread(it))
            it.save([flush: true])
        }
        if (!initialSpread) {
            message.nbSpread++
            message.author.score++
        }
        message.save([flush: true])

        // Envoie des notifications PUSH
        androidGcmService.notifyMessageSentTo(recipients)
        iosAPNSService.notifyMessageSentTo(recipients)
    }

    /**
     * Indique si le message a été reçus par l'utilisateur
     * @param user l'utilisateur concerné
     * @param messageId l'id du message concerné
     * @return Le résultat du test et le message
     */
    List isMessageReceivedByUser(User user, Long messageId) {
        List<Message> messagesReceivedByCurrentUser = (List<Message>) Message.createCriteria().list {
            receivedBy { eq('user.id', user.id) }
        }
        boolean receivedByThisUser = false
        Message message = null
        for (Message m : messagesReceivedByCurrentUser) {
            if (m.id == messageId) {
                message = m
                receivedByThisUser = true
                break
            }
        }
        return [receivedByThisUser, message]
    }

    void userSpreadThisMessage(User user, Message message) {
        message.removeFromReceivedBy(message.getReceivedFor(user))
        message.addToSpreadBy(new Spread(user))
        message.save([flush: true])
        spreadIt(message, false)
    }

    void userIgnoreThisMessage(User user, Message message) {
        message.removeFromReceivedBy(message.getReceivedFor(user))
        message.addToIgnoredBy(user)
        message.save([flush: true])
    }

    void userReportThisMessage(User user, Message message, ReportType reportType) {
        message.removeFromReceivedBy(message.getReceivedFor(user))
        message.addToReports(new Report(user, reportType))
        message.addToIgnoredBy(user)
        message.save([flush: true])

        message.author.reportsReceived++
        user.reportsSent++
    }

    /**
     * Suppression d'un message.<br>
     * Et notifications aux appli clientes
     * @param message
     */
    void deleteMessage(Message message) {
        notifyMessageWillDelete(message)
        message.delete([flush: true])
    }

    /**
     * Notifie les appli clientes de la suppression du message
     * @param message
     */
    void notifyMessageWillDelete(Message message) {
        Set<User> viewer = new HashSet<>()
        viewer.addAll(message.receivedBy.collect({ it.user }))
        viewer.addAll(message.spreadBy.collect({ it.user }))
        viewer.removeAll([null]) // supprime les éléments null qui débarque si une
        androidGcmService.notifyMessageDeleteTo(viewer, message.id)
        iosAPNSService.notifyMessageDeleteTo(viewer, message.id)
    }
}
