package it.uspread.core

import grails.converters.JSON
import grails.rest.RestfulController
import it.uspread.core.json.JSONAttribute
import it.uspread.core.json.JSONMarshaller
import it.uspread.core.params.MessageCriteria
import it.uspread.core.params.QueryParams
import it.uspread.core.type.ReportType

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
        boolean onlyDynamicVal = params.onlyDynamicVal != null ? new Boolean((String)params.onlyDynamicVal).booleanValue() : false
        // Vérifier que si le critère date est donné alors op est fourni
        if (params.date == null && params.op != null || params.op == null && params.date != null) {
            render([status: HttpStatus.BAD_REQUEST])
        }
        MessageCriteria msgCriteria = new MessageCriteria(params.count, params.date, params.op)

        // Si on liste les message reçus par l'utilisateur
        if (QueryParams.MESSAGE_RECEIVED.equals(query)) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST_RECEIVED) {
                respond(messageService.getMessagesReceivedByThisUserId(userConnected.id, msgCriteria))
            }
        }
        // Si on liste les messages écrits par l'utilisateur
        else if (QueryParams.MESSAGE_WRITED.equals(query)) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST) {
                respond(messageService.getMessagesWritedThisAuthorId(userConnected.id, msgCriteria))
            }
        }
        // Si on liste les message propagé par l'utilisateur
        else if (QueryParams.MESSAGE_SPREAD.equals(query)) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST) {
                respond(messageService.getMessagesSpreadByThisUserId(userConnected.id, msgCriteria))
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
        // Association de l'auteur du message (Car non renseigné dans le JSON)
        instance.author = userConnected
        instance.validate()

        if (instance.hasErrors()) {
            respond(instance.errors) // STATUS CODE 422
            return
        }

        instance.save([flush: true])

        // propagation initiale
        try {
            messageService.spreadIt(instance, true)
        } catch (Exception e) {
            respond 'error':e.getMessage(),status:422
            return
        }

        JSON.use(JSONMarshaller.PUBLIC_MESSAGE_CREATION) { respond(instance) }
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
            JSON.use(JSONMarshaller.PUBLIC_MESSAGE_SPREAD) { respond(message) }
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
            render([status: HttpStatus.NO_CONTENT])
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
        ReportType reportType = ReportType.valueOf(params.type)

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByThisUser(userConnected, messageId)
        if (receivedByThisUser && !userConnected.isModerator()) {
            messageService.userReportThisMessage(userConnected, message, reportType)
            render([status: HttpStatus.NO_CONTENT])
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
                respond(messageService.getMessagesWritedThisAuthorId(userId, null))
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
                respond(messageService.getMessagesReceivedByThisUserId(userId, null))
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
                respond(messageService.getMessagesSpreadByThisUserId(userId, null))
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
        Message message = queryForResource(params.id)
        if (null != message) {
            User userConnected = (User) springSecurityService.currentUser
            if (message.isUserAllowedToDelete(userConnected)) {
                messageService.deleteMessage(message)
                render([status: HttpStatus.NO_CONTENT])
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

    @Override
    protected getObjectToBind() {
        Message message = new Message()
        message.text = request.JSON.opt(JSONAttribute.MESSAGE_TEXT)
        message.textColor = request.JSON.opt(JSONAttribute.MESSAGE_TEXTCOLOR) ?: message.textColor
        message.backgroundColor = request.JSON.opt(JSONAttribute.MESSAGE_BACKGROUNDCOLOR) ?:  message.backgroundColor
        message.backgroundType = request.JSON.opt(JSONAttribute.MESSAGE_BACKGROUNDTYPE) ?: message.backgroundType
        return message
    }

}
