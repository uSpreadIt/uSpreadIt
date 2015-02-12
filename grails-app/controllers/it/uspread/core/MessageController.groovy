package it.uspread.core

import grails.converters.JSON
import grails.rest.RestfulController
import it.uspread.core.marshallers.JSONMarshaller

import org.springframework.http.HttpStatus

/**
 * Controlleur des accès aux messages
 */
class MessageController extends RestfulController<Message> {

    static scope = "singleton"
    static responseFormats = ["json"]

    def springSecurityService
    def messageService

    MessageController() {
        super(Message)
    }

    /**
     * Liste des messages de l'utilisateur connecté
     */
    @Override
    def index() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isModerator()) {
            forbidden()
            return
        }

        // Lecture des paramètres
        String query = params.query
        // TODO

        // Si on liste les messages écrits par l'utilisateur
        if ("AUTHOR".equals(query)) {
            JSON.use(JSONMarshaller.PUBLIC_MESSAGE_LIST_AUTHOR) {
                respond(messageService.getMessagesFromThisAuthorId(userConnected.id))
            }
        }
        // Si on liste les message reçus par l'utilisateur
        else if ("RECEIVED".equals(query)) {
            JSON.use(JSONMarshaller.PUBLIC_MESSAGE_LIST) {
                respond(messageService.getMessagesReceivedByThisUserId(userConnected.id))
            }
        }
        // Si on liste les message propagé par l'utilisateur
        else if ("SPREAD".equals(query)) {
            JSON.use(JSONMarshaller.PUBLIC_MESSAGE_LIST) {
                respond(messageService.getMessagesSpreadByThisUserId(userConnected.id))
            }
        }
        // Sinon retourner un code d'erreur
        else {
            render([status: HttpStatus.BAD_REQUEST])
        }
    }

    /**
     * Sauvegarde de création d'un message (POST)
     */
    @Override
    def save() {
        def userConnected = (User) springSecurityService.currentUser
        if (handleReadOnly() || userConnected.isModerator()) {
            return
        }

        // Vérification du quota
        if (messageService.isMessageCreationLimitReached(userConnected)) {
            render([status: 550, text: "Message Quota reached"])
            return
        }

        Message instance = createResource()
        instance.clearForCreation()
        // Association de l'auteur du message (Car non renseigné dans le JSON)
        instance.author = userConnected
        instance.validate()

        if (instance.hasErrors()) {
            respond(instance.errors) // STATUS CODE 422
            return
        }

        instance.save([flush: true])

        // propagation initiale
        messageService.spreadIt(instance, true)

        JSON.use(JSONMarshaller.PUBLIC_MESSAGE_CREATION) {
            respond(instance)
        }
    }

    /**
     * Propage le message dont l'id est fourni lors de l'appel
     */
    def spread() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        def messageId = params.messageId

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByThisUser(userConnected, messageId)
        if (receivedByThisUser && !userConnected.isModerator()) {
            messageService.userSpreadThisMessage(userConnected, message)
            JSON.use(JSONMarshaller.PUBLIC_MESSAGE_SPREAD) {
                respond(message)
            }
        } else {
            notFound()
        }
    }

    /**
     * Ignore le message dont l'id est fourni lors de l'appel
     */
    def ignore() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        def messageId = params.messageId

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByThisUser(userConnected, messageId)
        if (receivedByThisUser && !userConnected.isModerator()) {
            messageService.userIgnoreThisMessage(userConnected, message)

            request.withFormat {
                '*' { render([status: HttpStatus.NO_CONTENT]) }
            }
        } else {
            notFound()
        }
    }

    /**
     * Signale le message dont l'id est fourni lors de l'appel
     */
    def report() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        def messageId = params.messageId
        String type = params.type

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByThisUser(userConnected, messageId)
        if (receivedByThisUser && !userConnected.isModerator()) {
            messageService.userReportThisMessage(userConnected, message, type)
            request.withFormat {
                '*' { render([status: HttpStatus.NO_CONTENT]) }
            }
        } else {
            notFound()
        }
    }

    /**
     * Liste les messages écrits par l'utilisateur donné
     */
    def indexUserMsg() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        Long userId = ((String) params.userId).toLong()

        if (userConnected.isModerator()) {
            JSON.use(JSONMarshaller.INTERNAL) {
                respond(messageService.getMessagesFromThisAuthorId(userId))
            }
        } else {
            forbidden()
        }
    }

    /**
     * Liste les messages reçus par l'utilisateur donné
     */
    def indexUserMsgReceived() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        Long userId = ((String) params.userId).toLong()

        if (userConnected.isModerator()) {
            JSON.use(JSONMarshaller.INTERNAL) {
                respond(messageService.getMessagesReceivedByThisUserId(userId))
            }
        } else {
            forbidden()
        }
    }

    /**
     * Liste les messages propagé par l'utilisateur donné
     */
    def indexUserMsgSpread() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        Long userId = ((String) params.userId).toLong()

        if (userConnected.isModerator()) {
            JSON.use(JSONMarshaller.INTERNAL) {
                respond(messageService.getMessagesSpreadByThisUserId(userId))
            }
        } else {
            forbidden()
        }
    }

    /**
     * Liste tous les messages signalés
     */
    def indexMsgReported() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isModerator()) {
            JSON.use(JSONMarshaller.INTERNAL) {
                respond(messageService.getReportedMessages())
            }
        } else {
            forbidden()
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
            JSON.use(userConnected.isModerator() ? JSONMarshaller.INTERNAL : JSONMarshaller.PUBLIC_MESSAGE) {
                respond(queryForResource(params.id))
            }
        } else {
            forbidden()
        }
    }

    @Override
    def delete() {
        Message instance = queryForResource(params.id)
        if (null != instance) {
            User userConnected = (User) springSecurityService.currentUser
            if (instance.isUserAllowedToDelete(userConnected)) {
                super.delete()
            } else {
                forbidden()
            }
        } else {
            notFound()
        }
    }

    @Override
    def update() {
        // Non nécessaire pour le moment
    }

    def forbidden() {
        render([status: HttpStatus.FORBIDDEN])
    }

}
