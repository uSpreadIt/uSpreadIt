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
import it.uspread.core.params.URLParamsName
import it.uspread.core.params.URLParamsValue
import it.uspread.core.type.BackgroundType
import it.uspread.core.type.MessageType
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

        // Lecture des paramètres
        String query = params[URLParamsName.MESSAGE_QUERY]
        boolean onlyDynamicVal = params[URLParamsName.MESSAGE_ONLY_DYNAMICVALUE] != null ? new Boolean((String)params[URLParamsName.MESSAGE_ONLY_DYNAMICVALUE]).booleanValue() : false
        // Vérifier que si le critère date est donné alors op est fourni
        if (params[URLParamsName.MESSAGE_DATE] == null && params[URLParamsName.MESSAGE_OPERATOR] != null || params[URLParamsName.MESSAGE_OPERATOR] == null && params[URLParamsName.MESSAGE_DATE] != null) {
            return renderBadRequest()
        }
        MessageCriteria msgCriteria = new MessageCriteria(params[URLParamsName.MESSAGE_COUNT], params[URLParamsName.MESSAGE_DATE], params[URLParamsName.MESSAGE_OPERATOR])

        // Si on liste les message reçus par l'utilisateur
        if (URLParamsValue.MESSAGE_RECEIVED == query) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST_RECEIVED) {
                return respond(messageService.getMessagesReceivedByUserId(userConnected.id, msgCriteria), [status: HttpStatus.OK])
            }
        }
        // Si on liste les messages écrits par l'utilisateur
        else if (URLParamsValue.MESSAGE_WRITED == query) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST_WRITED) {
                return respond(messageService.getMessagesWritedByAuthorId(userConnected.id, msgCriteria), [status: HttpStatus.OK])
            }
        }
        // Si on liste les message propagé par l'utilisateur
        else if (URLParamsValue.MESSAGE_SPREAD == query) {
            JSON.use(onlyDynamicVal ? JSONMarshaller.PUBLIC_MESSAGE_LIST_DYNAMIC : JSONMarshaller.PUBLIC_MESSAGE_LIST_SPREAD) {
                return respond(messageService.getMessagesSpreadByUserId(userConnected.id, msgCriteria), [status: HttpStatus.OK])
            }
        }
        // Sinon retourner un code d'erreur
        else {
            return renderBadRequest()
        }
    }

    @Override
    def show() {
        Message message = queryForResource(params[URLParamsName.ID])
        if (message != null) {
            User userConnected = (User) springSecurityService.currentUser
            if (message.isUserAllowedToRead(userConnected)) {
                boolean onlyImage = params[URLParamsName.MESSAGE_ONLY_IMAGE] != null ? new Boolean((String)params[URLParamsName.MESSAGE_ONLY_IMAGE]).booleanValue() : false
                JSON.use(userConnected.isSpecialUser() ? JSONMarshaller.INTERNAL : (onlyImage ? JSONMarshaller.PUBLIC_MESSAGE_IMAGE : JSONMarshaller.PUBLIC_MESSAGE)) {
                    return respond(queryForResource(params[URLParamsName.ID]), [status: HttpStatus.OK])
                }
            } else {
                return renderForbidden()
            }
        } else {
            return renderNotFound()
        }
    }

    @Override
    def create() {
        // Non nécessaire car méthode d'obtention de formulaire de création.
    }

    /**
     * Sauvegarde de création d'un message (POST)
     */
    @Transactional
    @Override
    def save() {
        def userConnected = (User) springSecurityService.currentUser

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

        // propagation initiale
        messageService.spreadIt(newMessage, true)

        JSON.use(JSONMarshaller.PUBLIC_MESSAGE_CREATION) {
            return respond(newMessage, [status: HttpStatus.CREATED])
        }
    }

    @Override
    def edit() {
        // Non nécessaire car méthode d'obtention de formulaire d'édition.
    }

    @Override
    @Transactional
    def patch() {
        // Non nécessaire pour le moment
    }

    @Override
    @Transactional
    def update() {
        // Non nécessaire pour le moment
    }

    @Transactional
    @Override
    def delete() {
        Message message = queryForResource(params[URLParamsName.ID])
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

    /**
     * Propage le message dont l'id est fourni lors de l'appel
     */
    @Transactional
    def spread() {
        def userConnected = (User) springSecurityService.currentUser

        // Lecture des paramètres
        def messageId = new Long(params[URLParamsName.MESSAGE_ID])

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

        // Lecture des paramètres
        def messageId = new Long(params[URLParamsName.MESSAGE_ID])

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

        // Lecture des paramètres
        def messageId = new Long(params[URLParamsName.MESSAGE_ID])
        ReportType reportType
        try {
            reportType = ReportType.valueOf(params[URLParamsName.MESSAGE_REPORTTYPE])
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
        // Lecture des paramètres
        Long userId = ((String) params[URLParamsName.USER_ID]).toLong()

        JSON.use(JSONMarshaller.INTERNAL) {
            return respond(messageService.getMessagesWritedByAuthorId(userId, null), [status: HttpStatus.OK])
        }
    }

    /**
     * Liste les messages reçus par l'utilisateur donné
     */
    def indexUserMsgReceived() {
        // Lecture des paramètres
        Long userId = ((String) params[URLParamsName.USER_ID]).toLong()

        JSON.use(JSONMarshaller.INTERNAL) {
            return respond(messageService.getMessagesReceivedByUserId(userId, null), [status: HttpStatus.OK])
        }
    }

    /**
     * Liste les messages propagé par l'utilisateur donné
     */
    def indexUserMsgSpread() {
        // Lecture des paramètres
        Long userId = ((String) params[URLParamsName.USER_ID]).toLong()

        JSON.use(JSONMarshaller.INTERNAL) {
            return respond(messageService.getMessagesSpreadByUserId(userId, null), [status: HttpStatus.OK])
        }
    }

    /**
     * Liste tous les messages signalés
     */
    def indexMsgReported() {
        if (params[URLParamsName.USER_USERNAME]) {
            JSON.use(JSONMarshaller.INTERNAL) {
                return respond(messageService.getReportedMessages(userService.getUserFromUsername()), [status: HttpStatus.OK])
            }
        } else {
            JSON.use(JSONMarshaller.INTERNAL) {
                return respond(messageService.getReportedMessages(), [status: HttpStatus.OK])
            }
        }
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
            message.backgroundImage = new Image([image: Base64.decodeBase64(request.JSON.opt(JSONAttribute.MESSAGE_BACKGROUNDIMAGE))])
        } else {
            message.backgroundColor = request.JSON.opt(JSONAttribute.MESSAGE_BACKGROUNDCOLOR) ?:  'FFBB33'
        }
        message.link = request.JSON.opt(JSONAttribute.MESSAGE_LINK)
        message.type = request.JSON.opt(JSONAttribute.MESSAGE_TYPE) ?: MessageType.WORLD
        message.location = request.JSON.opt(JSONAttribute.MESSAGE_LOCATION)
        return message
    }

}
