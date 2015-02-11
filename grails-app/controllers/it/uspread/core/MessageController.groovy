package it.uspread.core

import grails.converters.JSON
import grails.rest.RestfulController
import it.uspread.core.marshallers.JSONMarshaller

import org.springframework.http.HttpStatus

class MessageController extends RestfulController<Message> {

    static scope = "singleton"
    static responseFormats = ["json"]

    def springSecurityService
    def messageService

    MessageController() {
        super(Message)
    }

    @Override
    def index() {
        def user = (User) springSecurityService.currentUser
        def type = params.query
        // Si on liste les messages de l'utilisateur (./message?query=AUTHOR)
        if ("AUTHOR".equals(type)) {
            JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond messageService.getMessagesFromThisAuthorId(user.id)
            }
        }
        // Si on liste les message reçus par l'utilisateur (./message?query=RECEIVED)
        else if ("RECEIVED".equals(type)) {
            JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond messageService.getMessagesReceivedByThisUserId(user.id)
            }
        }
        // Si on liste les message propagé par l'utilisateur (./message?query=SPREAD)
        else if ("SPREAD".equals(type)) {
            JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond messageService.getMessagesSpreadByThisUserId(user.id)
            }
        }
        // Sinon retourner une code d'erreur
        else {
            render([status: HttpStatus.BAD_REQUEST])
        }
    }

    /**
     * Sauvegarde de création d'un message (POST)
     */
    @Override
    def save() {
        def user = (User) springSecurityService.currentUser
        if (handleReadOnly() || user.isModerator()) {
            return
        }

        // Vérification du quota
        if (messageService.isMessageCreationLimitReached(user)) {
            render([status: 550, text: "Message Quota reached"])
            return
        }

        Message instance = createResource()
        instance.clearForCreation()
        // Association de l'auteur du message (Car non renseigné dans le JSON)
        instance.author = user
        instance.validate()

        if (instance.hasErrors()) {
            respond(instance.errors) // STATUS CODE 422
            return
        }

        instance.save([flush: true])

        // propagation initiale
        messageService.spreadIt(instance, true)

        request.withFormat {
            '*' {
                render([status: HttpStatus.CREATED, text:'{"id":"' + instance.id + '"}', contentType: "application/json", encoding: "UTF-8"])
            }
        }
    }

    /**
     * Propage le message dont l'id est fourni lors de l'appel
     */
    def spread() {
        def messageId = params.messageId
        def user = (User) springSecurityService.currentUser
        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByThisUser(user, messageId)
        if (receivedByThisUser && !user.isModerator()) {
            messageService.userSpreadThisMessage(user, message)
            request.withFormat {
                '*' {
                    render([status: HttpStatus.OK, text:'{"id":"' + message.id + '","nbSpread":"' + message.nbSpread + '"}', contentType: "application/json", encoding: "UTF-8"])
                }
            }
        } else {
            notFound()
        }
    }

    /**
     * Ignore le message dont l'id est fourni lors de l'appel
     */
    def ignore() {
        def messageId = params.messageId
        def user = (User) springSecurityService.currentUser
        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByThisUser(user, messageId)
        if (receivedByThisUser && !user.isModerator()) {
            messageService.userIgnoreThisMessage(user, message)

            request.withFormat {
                '*' { render status: HttpStatus.NO_CONTENT }
            }
        } else {
            notFound()
        }
    }

    /**
     * Signale le message dont l'id est fourni lors de l'appel
     */
    def report() {
        def messageId = params.messageId
        String type = params.type
        def user = (User) springSecurityService.currentUser
        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByThisUser(user, messageId)
        if (receivedByThisUser && !user.isModerator()) {
            messageService.userReportThisMessage(user, message, type)
            request.withFormat {
                '*' { render status: HttpStatus.NO_CONTENT } // NO CONTENT STATUS CODE
            }
        } else {
            notFound()
        }
    }

    /**
     * Liste les messages dont l'utilisateur donné est l'auteur (./users/$userId/messages)
     */
    def indexUserMsg() {
        Long userId = ((String) params.userId).toLong()
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isModerator()) {
            JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond messageService.getMessagesFromThisAuthorId(userId)
            }
        }
    }

    /**
     * Liste les messages reçus par l'utilisateur donné (./users/$userId/messages/received)
     */
    def indexUserMsgReceived() {
        Long userId = ((String) params.userId).toLong()
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isModerator()) {
            JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond messageService.getMessagesReceivedByThisUserId(userId)
            }
        }
    }

    /**
     * Liste les messages propagé par l'utilisateur donné (./users/$userId/messages/spread)
     */
    def indexUserMsgSpread() {
        Long userId = ((String) params.userId).toLong()
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isModerator()) {
            JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond messageService.getMessagesSpreadByThisUserId(userId)
            }
        }
    }

    /**
     * Liste tous les messages signalés
     * @return
     */
    def indexMsgReported() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isModerator()) {
            JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond messageService.getReportedMessages()
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
        if (null != instance && instance.isUserAllowedToRead(userConnected)) {
            JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond queryForResource(params.id)
            }
        } else {
            notFound()
        }
    }

    @Override
    def delete() {
        Message instance = queryForResource(params.id)
        if (null != instance) {
            User user = (User) springSecurityService.currentUser
            if (instance.isUserAllowedToDelete(user)) {
                super.delete()
            } else {
                notFound()
            }
        } else {
            notFound()
        }
    }

    @Override
    def update() {
        //TODO à autoriser que si modérateur
    }

}
