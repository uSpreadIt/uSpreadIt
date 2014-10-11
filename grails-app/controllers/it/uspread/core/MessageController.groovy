package it.uspread.core

import grails.rest.RestfulController

import org.codehaus.groovy.grails.web.servlet.HttpHeaders
import org.springframework.http.HttpStatus

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

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

	@Override
	def save() {
		if(handleReadOnly()) {
            return
        }
		//coucou
        Message instance = createResource()
        instance.clearForCreation()
		// AAAAAAAAAAA mode ultra batard pour mettre l'auteur
		instance.author = (User) springSecurityService.currentUser
        instance.validate()
        if (instance.hasErrors()) {
            respond instance.errors, view:'create' // STATUS CODE 422
            return
        }

        instance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: "${resourceName}.label".toString(), default: resourceClassName), instance.id])
                redirect instance
            }
            '*' {
                response.addHeader(HttpHeaders.LOCATION,
                        g.createLink(
                                resource: this.controllerName, action: 'show',id: instance.id, absolute: true,
                                namespace: hasProperty('namespace') ? this.namespace : null ))
                respond instance, [status: HttpStatus.CREATED]
            }
        }
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
        List<Message> messagesSentToCurrentUser = (List<Message>) Message.createCriteria().list {
            sentTo{ eq('id', user.id) }
        }
        boolean sentToThisUser = false
        Message message = null
		for (Message m : messagesSentToCurrentUser){
            if (m.id.equals(messageId.toLong())){
                message = m
                sentToThisUser = true
                break
            }
        }
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

	/**
	 * Signale le message dont l'id est fourni lors de l'appel
	 */
	def report() {
		def messageId = params.messageId
		def type = params.type
		def user = (User) springSecurityService.currentUser
		//TODO
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
		// Non nécessaire pour le moment
	}

	@Override
	def edit() {
		// Non nécessaire pour le moment
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
