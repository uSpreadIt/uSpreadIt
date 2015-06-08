package it.uspread.core.controller

import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.Transactional
import it.uspread.core.domain.Image
import it.uspread.core.domain.Message
import it.uspread.core.domain.User
import it.uspread.core.json.JSONAttribute
import it.uspread.core.json.JSONMarshaller
import it.uspread.core.params.MessageCriteria
import it.uspread.core.params.QueryParams
import it.uspread.core.type.BackgroundType
import it.uspread.core.type.ReportType

import org.apache.commons.codec.binary.Base64
import org.springframework.http.HttpStatus

/**
 * Controlleur des accès aux messages
 */
class MessageController extends RestfulController<Message> {

    static scope = 'singleton'
    static responseFormats = ['json']

    def springSecurityService
    def messageService
    def userService

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
            return renderForbidden()
        }

        // Lecture des paramètres
        String query = params.query
        boolean onlyDynamicVal = params.onlyDynamicVal != null ? new Boolean((String)params.onlyDynamicVal).booleanValue() : false
        // Vérifier que si le critère date est donné alors op est fourni
        if (params.date == null && params.op != null || params.op == null && params.date != null) {
            return renderBadRequest()
        }
        MessageCriteria msgCriteria = new MessageCriteria(params.count, params.date, params.op)

        // Si on liste les message reçus par l'utilisateur
        if (QueryParams.MESSAGE_RECEIVED == query) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST_RECEIVED) {
                return respond(messageService.getMessagesReceivedByUserId(userConnected.id, msgCriteria), [status: HttpStatus.OK])
            }
        }
        // Si on liste les messages écrits par l'utilisateur
        else if (QueryParams.MESSAGE_WRITED == query) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST_WRITED) {
                return respond(messageService.getMessagesWritedByAuthorId(userConnected.id, msgCriteria), [status: HttpStatus.OK])
            }
        }
        // Si on liste les message propagé par l'utilisateur
        else if (QueryParams.MESSAGE_SPREAD == query) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST_SPREAD) {
                return respond(messageService.getMessagesSpreadByUserId(userConnected.id, msgCriteria), [status: HttpStatus.OK])
            }
        }
        // Sinon retourner un code d'erreur
        else {
            return renderBadRequest()
        }
    }

    /**
     * Sauvegarde de création d'un message (POST)
     */
    @Override
    def save() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            return renderForbidden()
        }

        // Vérification du quota
        if (messageService.isMessageCreationLimitReached(userConnected)) {
            return render([status: 550, text: 'Message Quota reached'])
        }

        Message newMessage = createResource()
        // Association de l'auteur du message (Car non renseigné dans le JSON)
        newMessage.author = userConnected

        newMessage.validate()
        if (newMessage.hasErrors()) {
            return renderBadRequest()
        }

        newMessage.save([flush: true])

        try {
            // propagation initiale
            messageService.spreadIt(newMessage, true)
        } catch (Exception e) {// FIXME a retirer lorsque l'appli sera terminé
            return respond(JSON.parse("{'error':'${e.getMessage()?.encodeAsHTML()}'}"),[status:422])
        }


        JSON.use(JSONMarshaller.PUBLIC_MESSAGE_CREATION) {
            return respond(newMessage, [status: HttpStatus.CREATED])
        }
    }

    /**
     * Propage le message dont l'id est fourni lors de l'appel
     */
    @Transactional
    def spread() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            return renderForbidden()
        }

        // Lecture des paramètres
        def messageId = new Long(params.messageId)

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByUser(userConnected, messageId)
        if (receivedByThisUser) {
            messageService.userSpreadThisMessage(userConnected, message)
            JSON.use(JSONMarshaller.PUBLIC_MESSAGE_SPREAD) {
                return respond(message, [status: HttpStatus.ACCEPTED])
            }
        } else {
            return renderForbidden()
        }
    }

    /**
     * Ignore le message dont l'id est fourni lors de l'appel
     */
    @Transactional
    def ignore() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            return renderForbidden()
        }

        // Lecture des paramètres
        def messageId = new Long(params.messageId)

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByUser(userConnected, messageId)
        if (receivedByThisUser) {
            messageService.userIgnoreThisMessage(userConnected, message)
            return render([status: HttpStatus.ACCEPTED])
        } else {
            return renderForbidden()
        }
    }

    /**
     * Signale le message dont l'id est fourni lors de l'appel
     */
    @Transactional
    def report() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            return renderForbidden()
        }

        // Lecture des paramètres
        def messageId = new Long(params.messageId)
        ReportType reportType
        try {
            reportType = ReportType.valueOf(params.type)
        } catch (Exception e) {
            return renderBadRequest()
        }

        // On vérifie que le message a bien été reçu par l'utilisateur
        def (boolean receivedByThisUser, Message message) = messageService.isMessageReceivedByUser(userConnected, messageId)
        if (receivedByThisUser) {
            messageService.userReportThisMessage(userConnected, message, reportType)
            return render([status: HttpStatus.ACCEPTED])
        } else {
            return renderForbidden()
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
                return respond(messageService.getMessagesWritedByAuthorId(userId, null), [status: HttpStatus.OK])
            }
        } else {
            return renderForbidden()
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
                return respond(messageService.getMessagesReceivedByUserId(userId, null), [status: HttpStatus.OK])
            }
        } else {
            return renderForbidden()
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
                return respond(messageService.getMessagesSpreadByUserId(userId, null), [status: HttpStatus.OK])
            }
        } else {
            return renderForbidden()
        }
    }

    /**
     * Liste tous les messages signalés
     */
    def indexMsgReported() {
        def userConnected = (User) springSecurityService.currentUser
        if (userConnected.isSpecialUser()) {
            if (params.username) {
                JSON.use(JSONMarshaller.INTERNAL) {
                    return respond(messageService.getReportedMessages(userService.getUserFromUsername()), [status: HttpStatus.OK])
                }
            } else {
                JSON.use(JSONMarshaller.INTERNAL) {
                    return respond(messageService.getReportedMessages(), [status: HttpStatus.OK])
                }
            }
        } else {
            return renderForbidden()
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
                boolean onlyImage = params.onlyImage != null ? new Boolean((String)params.onlyImage).booleanValue() : false
                JSON.use(userConnected.isSpecialUser() ? JSONMarshaller.INTERNAL : (onlyImage ? JSONMarshaller.PUBLIC_MESSAGE_IMAGE : JSONMarshaller.PUBLIC_MESSAGE)) {
                    return respond(queryForResource(params.id), [status: HttpStatus.OK])
                }
            } else {
                return renderForbidden()
            }
        } else {
            return renderNotFound()
        }
    }

    @Override
    def delete() {
        Message message = queryForResource(params.id)
        if (null != message) {
            User userConnected = (User) springSecurityService.currentUser
            if (message.isUserAllowedToDelete(userConnected)) {
                messageService.deleteMessage(message)
                return render([status: HttpStatus.ACCEPTED])
            } else {
                return renderForbidden()
            }
        } else {
            return renderNotFound()
        }
    }

    @Override
    def update() {
        // Non nécessaire pour le moment
    }

    private def renderBadRequest() {
        return render([status: HttpStatus.BAD_REQUEST])
    }

    private def renderNotFound() {
        return render([status: HttpStatus.NOT_FOUND])
    }

    private def renderForbidden() {
        return render([status: HttpStatus.FORBIDDEN])
    }

    @Override
    protected getObjectToBind() {
        Message message = new Message()
        message.text = request.JSON.opt(JSONAttribute.MESSAGE_TEXT)
        message.textColor = request.JSON.opt(JSONAttribute.MESSAGE_TEXTCOLOR) ?: '000000'
        message.backgroundType = request.JSON.opt(JSONAttribute.MESSAGE_BACKGROUNDTYPE) ?: BackgroundType.PLAIN
        if (message.backgroundType == BackgroundType.IMAGE) {
            message.backgroundImage = new Image([image: Base64.decodeBase64(request.JSON.opt(JSONAttribute.MESSAGE_IMAGE))])
        } else {
            message.backgroundColor = request.JSON.opt(JSONAttribute.MESSAGE_BACKGROUNDCOLOR) ?:  'FFBB33'
        }
        return message
    }

}
