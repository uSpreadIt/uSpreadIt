package it.uspread.core

import grails.rest.RestfulController

import grails.converters.JSON
import grails.rest.RestfulController
import it.uspread.core.marshallers.JSONMarshaller

import org.springframework.http.HttpStatus

class MessageController extends RestfulController<Message> {

    // TODO à paramétrer
    private static final int SPREAD_SIZE = 10
    private static final int QUOTA = 1000
    static scope = "singleton"
    static responseFormats = ["json"]

    def springSecurityService

    MessageController() {
        super(Message)
    }

    @Override
    def index() {
        def user = (User) springSecurityService.currentUser
        def type = params.query
        // Si on liste les messages de l'utilisateur (./message ou ./message?query=AUTHOR)
        if (MessageQuery.AUTHOR.name().equals(type) || type == null) {
            //TODO peut être mieux de simplement pas autoriser l'url ./message ?
            JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond Message.where { author.id == user.id }.list()
            }
        }
        // Si on liste les message reçus par l'utilisateur (./message?query=RECEIVED)
        else if (MessageQuery.RECEIVED.name().equals(type)) {
            JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond Message.createCriteria().list {
                    sentTo{ eq('id', user.id) }
                }
            }
        }
        // Si on liste les message propagé par l'utilisateur (./message?query=SPREAD)
        else if (MessageQuery.SPREAD.name().equals(type)) {
            JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond Message.createCriteria().list {
                    spreadBy{ eq('id', user.id) }
                }
            }
        }
        // Sinon retourner une code d'erreur
        else {
            render([status:HttpStatus.BAD_REQUEST])
        }
    }

    /**
     * Sauvegarde de création d'un message (POST)
     */
    @Override
    def save() {
        if(handleReadOnly()) {
            return
        }

        // Vérification du quota
        if (limitReached()) {
            render([status:550, text:"Message Quota reached"])
            return
        }

        Message instance = createResource()
        instance.clearForCreation()
        // Association de l'auteur du message (Car non renseigné dans le JSON)
        instance.author = (User) springSecurityService.currentUser
        instance.validate()

        if (instance.hasErrors()) {
            respond(instance.errors) // STATUS CODE 422
            return
        }

        instance.save([flush:true])

        // propagation initiale
        spreadIt(instance, SPREAD_SIZE, true)

        request.withFormat {
            '*' {
                render([status:HttpStatus.CREATED])
            }
        }
    }

    /**
     * Propagation d'un message
     * @param message le message à propager
     * @param spreadSize le nombre de personnes qui recevront le message
     * @param initialSpread pour distinguer la création d'un nouveau message de la propagation
     */
    private static void spreadIt(Message message, int spreadSize, boolean initialSpread) {
        // Select spreadSize users order by lastReceivedMessageDate asc
        List<User> recipients
        if (initialSpread) {
            recipients = User.findAllBySpecialUserAndIdNotLike(
                    false, message.authorId, [max: spreadSize, sort: 'lastReceivedMessageDate', order: 'asc'])
        } else {
            def usersWhoReceivedThisMessage = message.ignoredBy.collect {it.id}
            usersWhoReceivedThisMessage.addAll(message.sentTo.collect{it.id})
            usersWhoReceivedThisMessage.addAll(message.spreadBy.collect{it.id})

            recipients = User.findAllBySpecialUserAndIdNotLikeAndIdNotInList(
                    false, message.authorId, usersWhoReceivedThisMessage, [max: spreadSize, sort: 'lastReceivedMessageDate', order: 'asc'])
        }
        recipients = recipients.size() >= spreadSize ? recipients[0..spreadSize - 1] : recipients
        Date now = new Date()
        recipients.each {
            it.lastReceivedMessageDate = now
            message.addToSentTo(it)
            it.save(flush: true)
        }
        if (!initialSpread) {
            message.nbSpread++
            message.author.score++
            //Test rebase
        }
        message.save(flush: true)
    }

    /**
     * Indique si le quota de nouveau message de l'utilisateur est atteint.<br>
     * Sur les dernières 24 heures on limite à 2 messages max.
     * TODO Cette vérification devra être géré de façon atomique (possible en prenant en compte la scalabilité des serveurs ?)
     * @return true si quota atteint
     */
    def limitReached() {
        def startDate = (new Date()).minus(1);
        def nbMessage = Message.where{ author.id == ((User) springSecurityService.currentUser).id && dateCreated > startDate }.count()
        return nbMessage >= QUOTA
    }

    /**
     * Propage le message dont l'id est fourni lors de l'appel
     */
    def spread() {
        def messageId = params.messageId
        def user = (User) springSecurityService.currentUser
        // On vérifie que le message est bien reçu par l'utilisateur
        def (boolean sentToThisUser, Message message) = isMessageSentToThisUser(user, messageId)
        if (sentToThisUser){
            message.sentTo.remove(user)
            message.spreadBy.add(user)
            spreadIt(message, SPREAD_SIZE, false)

            request.withFormat {
                form multipartForm {
                    flash.message = message(code: 'default.deleted.message', args: [
                        message(code: "${resourceClassName}.label".toString(), default: resourceClassName),
                        messageId
                    ])
                    redirect action:"index", method:"GET"
                }
                '*'{ render status: HttpStatus.NO_CONTENT } // NO CONTENT STATUS CODE
            }
        }
        else {
            notFound()
        }
    }

    /**
     * Ignore le message dont l'id est fourni lors de l'appel
     */
    def ignore() {
        def messageId = params.messageId
        def user = (User) springSecurityService.currentUser
        // On vérifie que le message est bien reçu par l'utilisateur
        def (boolean sentToThisUser, Message message) = isMessageSentToThisUser(user, messageId)
        if (sentToThisUser){
            message.sentTo.remove(user)
            message.ignoredBy.add(user)
            message.save(flush: true)

            request.withFormat {
                form multipartForm {
                    flash.message = message(code: 'default.deleted.message', args: [
                        message(code: "${resourceClassName}.label".toString(), default: resourceClassName),
                        messageId
                    ])
                    redirect action:"index", method:"GET"
                }
                '*'{ render status: HttpStatus.NO_CONTENT } // NO CONTENT STATUS CODE
            }
        }
        else {
            notFound()
        }
    }

    private List isMessageSentToThisUser(user, messageId) {
        List<Message> messagesSentToCurrentUser = (List<Message>) Message.createCriteria().list {
            sentTo { eq('id', user.id) }
        }
        boolean sentToThisUser = false
        Message message = null
        for (Message m : messagesSentToCurrentUser) {
            if (m.id.equals(messageId.toLong())) {
                message = m
                sentToThisUser = true
                break
            }
        }
        return [sentToThisUser, message]
    }

    /**
     * Signale le message dont l'id est fourni lors de l'appel
     */
    def report() {
        def messageId = params.messageId
        // TODO non utilisé pour l'instant
        def type = params.type
        def user = (User) springSecurityService.currentUser
        // On vérifie que le message est bien reçu par l'utilisateur
        def (boolean sentToThisUser, Message message) = isMessageSentToThisUser(user, messageId)
        if (sentToThisUser){
            message.sentTo.remove(user)
            message.reportedBy.add(user)
            message.ignoredBy.add(user)
            message.save(flush: true)

            request.withFormat {
                form multipartForm {
                    flash.message = message(code: 'default.deleted.message', args: [
                        message(code: "${resourceClassName}.label".toString(), default: resourceClassName),
                        messageId
                    ])
                    redirect action:"index", method:"GET"
                }
                '*'{ render status: HttpStatus.NO_CONTENT } // NO CONTENT STATUS CODE
            }
        }
        else {
            notFound()
        }
    }

    /**
     * Liste les messages dont l'utilisateur donné est l'auteur (./users/$userId/messages)
     */
    def indexUserMsg() {
        def userId = params.userId
        def userConnected = (User) springSecurityService.currentUser
        JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
            respond Message.where { author.id == userId }.list()
        }
    }

    /**
     * Liste les messages reçus par l'utilisateur donné (./users/$userId/messages/received)
     */
    def indexUserMsgReceived() {
        def userId = params.userId
        def userConnected = (User) springSecurityService.currentUser
        JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
            respond Message.createCriteria().list {
                sentTo{ eq('id', userId.toLong()) }
            }
        }
    }

    /**
     * Liste les messages propagé par l'utilisateur donné (./users/$userId/messages/spread)
     */
    def indexUserMsgSpread() {
        def userId = params.userId
        def userConnected = (User) springSecurityService.currentUser
        JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
            respond Message.createCriteria().list {
                spreadBy{ eq('id', userId.toLong()) }
            }
        }
    }

    /**
     * Liste tous les messages signalés
     * @return
     */
    def indexMsgReported() {
        def userConnected = (User) springSecurityService.currentUser
        JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
            respond Message.createCriteria().list {
                reportedBy{ isNotNull('id') }
            }
        }
    }

    @Override
    def create() {
        // Non nécessaire car méthode d'obtention de formulaire de création.
    }

    @Override
    def edit() {
        // Non nécessaire car méthode d'obtention de formulaire d'édition.
    }

    @Override
    def patch() {
        // Non nécessaire pour le moment
    }

    @Override
    def show() {
        User userConnected = (User) springSecurityService.currentUser
        Message instance = queryForResource(params.id)
        if (null != instance && instance.isUserAllowedToRead(userConnected)){
            JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond queryForResource(params.id)
            }
        }
        else {
            notFound()
        }
    }

    @Override
    def delete() {
        Message instance = queryForResource(params.id)
        if (null != instance){
            User user = (User) springSecurityService.currentUser
            if (instance.isUserAllowedToDelete(user)){
                super.delete()
            }
            else {
                notFound()
            }
        }
        else {
            notFound()
        }
    }

    @Override
    def update() {
        //TODO à autoriser que si modérateur
    }
}
