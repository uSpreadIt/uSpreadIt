package it.uspread.core.controller

import grails.converters.JSON
import grails.rest.RestfulController
import it.uspread.core.domain.Message;
import it.uspread.core.domain.User;
import it.uspread.core.json.JSONAttribute
import it.uspread.core.json.JSONMarshaller
import it.uspread.core.params.MessageCriteria
import it.uspread.core.params.QueryParams
import it.uspread.core.type.BackgroundType
import it.uspread.core.type.ReportType

import org.springframework.http.HttpStatus

/**
 * Controlleur des accès aux messages
 */
class MessageController extends RestfulController<Message> {

    static scope = 'singleton'
    static responseFormats = ['json']

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
        if (userConnected.isSpecialUser()) {
            renderForbidden()
            return
        }

        // Lecture des paramètres
        String query = params.query
        boolean onlyDynamicVal = params.onlyDynamicVal != null ? new Boolean((String)params.onlyDynamicVal).booleanValue() : false
        // Vérifier que si le critère date est donné alors op est fourni
        if (params.date == null && params.op != null || params.op == null && params.date != null) {
            renderBadRequest()
            return
        }
        MessageCriteria msgCriteria = new MessageCriteria(params.count, params.date, params.op)

        // Si on liste les message reçus par l'utilisateur
        if (QueryParams.MESSAGE_RECEIVED == query) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST_RECEIVED) {
                respond(messageService.getMessagesReceivedByUserId(userConnected.id, msgCriteria), [status: HttpStatus.OK])
            }
        }
        // Si on liste les messages écrits par l'utilisateur
        else if (QueryParams.MESSAGE_WRITED == query) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST) {
                respond(messageService.getMessagesWritedByAuthorId(userConnected.id, msgCriteria), [status: HttpStatus.OK])
            }
        }
        // Si on liste les message propagé par l'utilisateur
        else if (QueryParams.MESSAGE_SPREAD == query) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST) {
                respond(messageService.getMessagesSpreadByUserId(userConnected.id, msgCriteria), [status: HttpStatus.OK])
            }
        }
        // Sinon retourner un code d'erreur
        else {
            renderBadRequest()
        }
    }

    /**
     * Sauvegarde de création d'un message (POST)
     */
    @Override
    def save() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            renderForbidden()
            return
        }

        // Vérification du quota
        if (messageService.isMessageCreationLimitReached(userConnected)) {
            render([status: 550, text: 'Message Quota reached'])
            return
        }

        Message newMessage = createResource()
        // Association de l'auteur du message (Car non renseigné dans le JSON)
        newMessage.author = userConnected

        // FIXME temporaire pour que le client ios fonctionne
        if (newMessage.backgroundType == null) {
            newMessage.backgroundType = BackgroundType.PLAIN
            newMessage.backgroundColor = 'FFBB33'
            newMessage.textColor = '000000'
        }

        newMessage.validate()
        if (newMessage.hasErrors()) {
            renderBadRequest()
            return
        }

        newMessage.save([flush: true])

        // propagation initiale
        messageService.spreadIt(newMessage, true)

        JSON.use(JSONMarshaller.PUBLIC_MESSAGE_CREATION) {
            respond(newMessage, [status: HttpStatus.CREATED])
        }
    }

    /**
     * Propage le message dont l'id est fourni lors de l'appel
     */
    def spread() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            renderForbidden()
            return
        }

        // Lecture des paramètres
        def messageId = new Long(params.messageId)

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByUser(userConnected, messageId)
        if (receivedByThisUser) {
            messageService.userSpreadThisMessage(userConnected, message)
            JSON.use(JSONMarshaller.PUBLIC_MESSAGE_SPREAD) {
                respond(message, [status: HttpStatus.ACCEPTED])
            }
        } else {
            renderForbidden()
        }
    }

    /**
     * Ignore le message dont l'id est fourni lors de l'appel
     */
    def ignore() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            renderForbidden()
            return
        }

        // Lecture des paramètres
        def messageId = new Long(params.messageId)

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByUser(userConnected, messageId)
        if (receivedByThisUser) {
            messageService.userIgnoreThisMessage(userConnected, message)
            render([status: HttpStatus.ACCEPTED])
        } else {
            renderForbidden()
        }
    }

    /**
     * Signale le message dont l'id est fourni lors de l'appel
     */
    def report() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            renderForbidden()
            return
        }

        // Lecture des paramètres
        def messageId = new Long(params.messageId)
        ReportType reportType
        try {
            reportType = ReportType.valueOf(params.type)
        } catch (Exception e) {
            renderBadRequest()
            return
        }

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByUser(userConnected, messageId)
        if (receivedByThisUser) {
            messageService.userReportThisMessage(userConnected, message, reportType)
            render([status: HttpStatus.ACCEPTED])
        } else {
            renderForbidden()
        }
    }

    /**
     * Liste les messages écrits par l'utilisateur donné
     */
    def indexUserMsg() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        Long userId = ((String) params.userId).toLong()

        if (userConnected.isSpecialUser()) {
            JSON.use(JSONMarshaller.INTERNAL) {
                respond(messageService.getMessagesWritedByAuthorId(userId, null), [status: HttpStatus.OK])
            }
        } else {
            renderForbidden()
        }
    }

    /**
     * Liste les messages reçus par l'utilisateur donné
     */
    def indexUserMsgReceived() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        Long userId = ((String) params.userId).toLong()

        if (userConnected.isSpecialUser()) {
            JSON.use(JSONMarshaller.INTERNAL) {
                respond(messageService.getMessagesReceivedByUserId(userId, null), [status: HttpStatus.OK])
            }
        } else {
            renderForbidden()
        }
    }

    /**
     * Liste les messages propagé par l'utilisateur donné
     */
    def indexUserMsgSpread() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        Long userId = ((String) params.userId).toLong()

        if (userConnected.isSpecialUser()) {
            JSON.use(JSONMarshaller.INTERNAL) {
                respond(messageService.getMessagesSpreadByUserId(userId, null), [status: HttpStatus.OK])
            }
        } else {
            renderForbidden()
        }
    }

    /**
     * Liste tous les messages signalés
     */
    def indexMsgReported() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            JSON.use(JSONMarshaller.INTERNAL) {
                respond(messageService.getReportedMessages(), [status: HttpStatus.OK])
            }
        } else {
            renderForbidden()
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
        Message message = queryForResource(params.id)
        if (message != null) {
            User userConnected = (User) springSecurityService.currentUser
            if (message.isUserAllowedToRead(userConnected)) {
                JSON.use(userConnected.isSpecialUser() ? JSONMarshaller.INTERNAL : JSONMarshaller.PUBLIC_MESSAGE) {
                    respond(queryForResource(params.id), [status: HttpStatus.OK])
                }
            } else {
                renderForbidden()
            }
            return
        } else {
            renderNotFound()
        }
    }

    @Override
    def delete() {
        Message message = queryForResource(params.id)
        if (null != message) {
            User userConnected = (User) springSecurityService.currentUser
            if (message.isUserAllowedToDelete(userConnected)) {
                messageService.deleteMessage(message)
                render([status: HttpStatus.ACCEPTED])
            } else {
                renderForbidden()
            }
        } else {
            renderNotFound()
        }
    }

    @Override
    def update() {
        // Non nécessaire pour le moment
    }

    private def renderBadRequest() {
        render([status: HttpStatus.BAD_REQUEST])
    }

    private def renderNotFound() {
        render([status: HttpStatus.NOT_FOUND])
    }

    private def renderForbidden() {
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
