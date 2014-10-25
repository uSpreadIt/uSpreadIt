package it.uspread.core

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import grails.rest.RestfulController

import org.springframework.http.HttpStatus

class MessageController extends RestfulController<Message> {

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
			respond Message.where { author.id == user.id }.list()

			respond User.list(max: spreadSize, sort: 'lastReceivedMessageDate', order: 'asc')
		}
		// Si on liste les message reçus par l'utilisateur (./message?query=RECEIVED)
		else if (MessageQuery.RECEIVED.name().equals(type)) {
			respond Message.createCriteria().list {
				sentTo{ eq('id', user.id) }
			}
		}
		// Si on liste les message propagé par l'utilisateur (./message?query=SPREAD)
		else if (MessageQuery.SPREAD.name().equals(type)) {
			respond Message.createCriteria().list {
				spreadBy{ eq('id', user.id) }
			}
		}
		// Sinon retourner une erreur ?
		else {
			// TODO
			render "Bloquer ça"
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

        Message instance = createResource()
        instance.clearForCreation()
		// Association de l'auteur du message (Car non renseigné dans le JSON)
		instance.author = (User) springSecurityService.currentUser
        instance.validate()

		if (limitReached()) {
			render status: 442 // TODO mettre statut spécifique a notre usage
			return
		}


        if (instance.hasErrors()) {
            respond instance.errors // STATUS CODE 422
            return
        }

        instance.save flush:true

        request.withFormat {
            '*' {
				render status: HttpStatus.CREATED
            }
        }
	}

	/**
	 * QUICK TEST CREATION LIMIT : Pour le test sur les dernières 24H on limite à 2 messages max
	 * @return
	 */
	def limitReached() {
		def startDate = (new Date()).minus(1);
		List<Message> listMessage = Message.where({ author.id == ((User) springSecurityService.currentUser).id && dateCreated > startDate }).list()
		return listMessage.size() >= 1000
	}

	/**
	 * Propage le message dont l'id est fourni lors de l'appel
	 */
	def spread() {
		def messageId = params.messageId
		def user = (User) springSecurityService.currentUser
		//TODO
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
            message.save(flush: true)

            request.withFormat {
                form multipartForm {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: "${resourceClassName}.label".toString(), default: resourceClassName), messageId])
                    redirect action:"index", method:"GET"
                }
                '*'{ render status: NO_CONTENT } // NO CONTENT STATUS CODE
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
		def type = params.type
		def user = (User) springSecurityService.currentUser
        // On vérifie que le message est bien reçu par l'utilisateur
        def (boolean sentToThisUser, Message message) = isMessageSentToThisUser(user, messageId)
        if (sentToThisUser){
            message.sentTo.remove(user)
            message.reportedBy.add(user)
            message.save(flush: true)

            request.withFormat {
                form multipartForm {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: "${resourceClassName}.label".toString(), default: resourceClassName), messageId])
                    redirect action:"index", method:"GET"
                }
                '*'{ render status: NO_CONTENT } // NO CONTENT STATUS CODE
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
		respond Message.where { author.id == userId }.list()
	}

	/**
	 * Liste les messages reçus par l'utilisateur donné (./users/$userId/messages/received)
	 */
	def indexUserMsgReceived() {
		def userId = params.userId
		respond Message.createCriteria().list {
			sentTo{ eq('id', userId.toLong()) }
		}
	}

	/**
	 * Liste les messages propagé par l'utilisateur donné (./users/$userId/messages/spread)
	 */
	def indexUserMsgSpread() {
		def userId = params.userId
		respond Message.createCriteria().list {
			spreadBy{ eq('id', userId.toLong()) }
		}
	}

	/**
	 * Liste tous les messages signalés
	 * @return
	 */
	def indexMsgReported() {
        respond Message.createCriteria().list {
            reportedBy{ isNotNull('id') }
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
        User user = (User) springSecurityService.currentUser
        Message instance = queryForResource(params.id)
        if (null != instance && instance.isUserAllowedToRead(user)){
            super.show()
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
