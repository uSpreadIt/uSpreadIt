package it.uspread.core

import grails.transaction.Transactional
import it.uspread.core.data.Status
import it.uspread.core.params.MessageCriteria
import it.uspread.core.params.QueryParams
import it.uspread.core.type.ReportType

@Transactional
class MessageService {

    def APNSMessageService
    def androidGcmPushService

    // TODO à paramétrer
    private static final int MAX_MESSAGES_PER_DAY = 1000
    private static final int SPREAD_SIZE = 10

    /**
     * Recherche les messages écrits par un utilisateur
     * @param id Id de l'auteur des messages
     * @param msgCriteria Critères de recherche supplémentaires
     * @return liste de messages
     */
    public List<Message> getMessagesWritedThisAuthorId(Long id, MessageCriteria msgCriteria) {
        def listMap = [sort: "dateCreated", order: "desc"]

        if (msgCriteria != null && msgCriteria.getCount() > 0) {
            listMap["max"] = msgCriteria.getCount()
        }

        List<Message> listMessage;
        // Recherche des messages avec critère de date
        if (msgCriteria != null && msgCriteria.getOperator() != null) {
            if (QueryParams.OPERATOR_GREATER.equals(msgCriteria.getOperator())) {
                listMessage = Message.where {
                    author.id == id && dateCreated > msgCriteria.getDate()
                }.list(listMap)
            } else if (QueryParams.OPERATOR_GREATER_OR_EQUALS.equals(msgCriteria.getOperator())) {
                listMessage = Message.where {
                    author.id == id && dateCreated >= msgCriteria.getDate()
                }.list(listMap)
            } else {
                listMessage = Message.where {
                    author.id == id && dateCreated < msgCriteria.getDate()
                }.list(listMap)
            }
        }
        // Recherche des messages sans critère de date
        else {
            listMessage = Message.where { author.id == id }.list(listMap)
        }
        return listMessage
    }

    /**
     * Recherche les messages reçus par un utilisateur
     * @param id Id de l'utilisateur recevant les messages
     * @param msgCriteria Critères de recherche supplémentaires
     * @return liste de messages
     */
    public List<Message> getMessagesReceivedByThisUserId(Long id, MessageCriteria msgCriteria) {
        def listMap = [:]
        if (msgCriteria != null && msgCriteria.getCount() > 0) {
            listMap["max"] = msgCriteria.getCount()
        }

        List<Message> listMessage;
        // Recherche des messages avec critère de date
        if (msgCriteria != null && msgCriteria.getOperator() != null) {
            if (QueryParams.OPERATOR_GREATER.equals(msgCriteria.getOperator())) {
                listMessage =  Message.createCriteria().list(listMap, {
                    receivedBy {
                        eq("user.id", id)
                        gt("date", msgCriteria.getDate())
                        order("date", "desc")
                    }
                })
            } else if (QueryParams.OPERATOR_GREATER_OR_EQUALS.equals(msgCriteria.getOperator())) {
                listMessage =  Message.createCriteria().list(listMap, {
                    receivedBy {
                        eq("user.id", id)
                        ge("date", msgCriteria.getDate())
                        order("date", "desc")
                    }
                })
            } else {
                listMessage =  Message.createCriteria().list(listMap, {
                    receivedBy {
                        eq('user.id', id)
                        lt("date", msgCriteria.getDate())
                        order("date", "desc")
                    }
                })
            }
        }
        // Recherche des messages sans critère de date
        else {
            listMessage =  Message.createCriteria().list(listMap, {
                receivedBy {
                    eq("user.id", id)
                    order("date", "desc")
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
    public List<Message> getMessagesSpreadByThisUserId(Long id, MessageCriteria msgCriteria) {
        def listMap = [:]
        if (msgCriteria != null && msgCriteria.getCount() > 0) {
            listMap["max"] = msgCriteria.getCount()
        }

        List<Message> listMessage;
        // Recherche des messages avec critère de date
        if (msgCriteria != null && msgCriteria.getOperator() != null) {
            if (QueryParams.OPERATOR_GREATER.equals(msgCriteria.getOperator())) {
                listMessage =  Message.createCriteria().list(listMap, {
                    spreadBy {
                        eq("user.id", id)
                        gt("date", msgCriteria.getDate())
                        order("date", "desc")
                    }
                })
            } else if (QueryParams.OPERATOR_GREATER_OR_EQUALS.equals(msgCriteria.getOperator())) {
                listMessage =  Message.createCriteria().list(listMap, {
                    spreadBy {
                        eq("user.id", id)
                        ge("date", msgCriteria.getDate())
                        order("date", "desc")
                    }
                })
            } else {
                listMessage =  Message.createCriteria().list(listMap, {
                    spreadBy {
                        eq('user.id', id)
                        lt("date", msgCriteria.getDate())
                        order("date", "desc")
                    }
                })
            }
        }
        // Recherche des messages sans critère de date
        else {
            listMessage =  Message.createCriteria().list(listMap, {
                spreadBy {
                    eq("user.id", id)
                    order("date", "desc")
                }
            })
        }
        return listMessage
    }

    /**
     * Recherche des messages signalés
     * @return liste de messages
     */
    public List<Message> getReportedMessages() {
        return (List<Message>) Message.createCriteria().list {
            reports { isNotNull('id') }
        }
    }

    /**
     * Retourne les informations de status de l'utilisateur vis à vis des messages
     * @param user
     * @return
     */
    def getUserMessagesStatus(User user, boolean quotaOnly) {
        Status status = new Status()
        status.setQuotaReached(isMessageCreationLimitReached(user))
        if (!quotaOnly) {
            status.setNbMessageWrited(Message.createCriteria().count({eq("author.id", user.id)}))
            status.setNbMessageSpread(Message.createCriteria().count({spreadBy{eq("user.id", user.id)}}))
            status.setNbMessageIgnored(Message.createCriteria().count({ignoredBy{eq("id", user.id)}}))
            status.setNbMessageReported(Message.createCriteria().count({reports{eq("reporter.id", user.id)}}))
        }
        return status
    }

    /**
     * Indique si le quota de nouveau message de l'utilisateur est atteint.<br>
     * Sur les dernières 24 heures on limite à 2 messages max.
     * TODO Cette vérification devra être géré de façon atomique (possible en prenant en compte la scalabilité des serveurs ?)
     * @return true si quota atteint
     */
    def isMessageCreationLimitReached(User user) {
        def startDate = (new Date()).minus(1);
        def nbMessage = Message.where { author.id == user.id && dateCreated > startDate }.count()
        return nbMessage >= MAX_MESSAGES_PER_DAY
    }

    /**
     * Propagation d'un message
     * @param message le message à propager
     * @param initialSpread pour distinguer la création d'un nouveau message de la propagation
     */
    public void spreadIt(Message message, boolean initialSpread) {
        // Select spreadSize users order by lastReceivedMessageDate asc
        List<User> recipients
        if (initialSpread) {
            recipients = User.findAllBySpecialUserAndIdNotEqual(
                    false, message.authorId, [max: SPREAD_SIZE, sort: 'lastReceivedMessageDate', order: 'asc'])
        } else {
            def usersWhoReceivedThisMessage = message.ignoredBy.collect { it.id }
            usersWhoReceivedThisMessage.addAll(message.receivedBy.collect { it.user.id })
            usersWhoReceivedThisMessage.addAll(message.spreadBy.collect { it.user.id })

            recipients = User.findAllBySpecialUserAndIdNotEqualAndIdNotInList(
                    false, message.authorId, usersWhoReceivedThisMessage, [max: SPREAD_SIZE, sort: 'lastReceivedMessageDate', order: 'asc'])
        }
        recipients = recipients.size() >= SPREAD_SIZE ? recipients[0..SPREAD_SIZE - 1] : recipients
        Date now = new Date()
        recipients.each {
            it.lastReceivedMessageDate = now
            message.addToReceivedBy(new Spread(it))
            it.save(flush: true)
        }
        if (!initialSpread) {
            message.nbSpread++
            message.author.score++
            //Test rebase
        }
        message.save(flush: true)

        // Envoie des notifications PUSH
        this.APNSMessageService.notifySentTo(recipients)
        androidGcmPushService.notifyMessageSentTo(recipients)
    }

    public List isMessageReceivedByThisUser(user, messageId) {
        List<Message> messagesReceivedByCurrentUser = (List<Message>) Message.createCriteria().list {
            receivedBy { eq('user.id', user.id) }
        }
        boolean receivedByThisUser = false
        Message message = null
        for (Message m : messagesReceivedByCurrentUser) {
            if (m.id.equals(messageId.toLong())) {
                message = m
                receivedByThisUser = true
                break
            }
        }
        return [receivedByThisUser, message]
    }

    public void userSpreadThisMessage(User user, Message message) {
        message.receivedBy.remove(new Spread(user))
        message.spreadBy.add(new Spread(user, new Date()))
        spreadIt(message, false)
    }

    public void userIgnoreThisMessage(User user, Message message) {
        message.receivedBy.remove(new Spread(user))
        message.ignoredBy.add(user)
        message.save(flush: true)
    }

    public void userReportThisMessage(User user, Message message, ReportType reportType) {
        message.receivedBy.remove(new Spread(user))
        message.reports.add(new Report(user, reportType))
        message.incrementReportType(reportType)
        message.ignoredBy.add(user)
        message.author.reportsReceived++
        user.reportsSent++
        message.save(flush: true)
        user.save(flush: true)
    }

    /**
     * Suppression d'un message.<br>
     * S'accompagne d'une notification PUSH pour indiquer aux utilisateurs ayant reçus ou propagé ce message qu'il doit disparaitre
     * @param message
     */
    public void deleteMessage(Message message) {
        List<User> viewer = new ArrayList<>()
        viewer.addAll(message.receivedBy.collect { it.user })
        viewer.addAll(message.spreadBy.collect { it.user })
        viewer.removeAll([null])
        message.delete(flush: true)
        androidGcmPushService.notifyMessageDeleteTo(viewer, message)
    }
}
