package it.uspread.core

import grails.rest.RestfulController

class MessageController extends RestfulController {

	static scope = "singleton"
	static responseFormats = ["json"]

	def springSecurityService

	MessageController() {
		super(Message)
	}

	@Override
	def index() {
		def user = (User) springSecurityService.currentUser
		def type = params.type
		// Si on liste les messages de l'utilisateur (./message ou ./message?type=AUTHOR)
		if (MessageType.AUTHOR.name().equals(type) || type == null) {
			//TODO peut être mieux de simplement pas autoriser l'url ./message ?
			respond Message.where { author.id == user.id }.list()
		}
		// Si on liste les message en attente de décision de propagation pour l'utilisateur (./message?type=TO_SENT)
		else if (MessageType.TO_SENT.name().equals(type)) {
			respond Message.createCriteria().list {
				sentTo{ eq('id', user.id) }
			}
		}
		// Si on liste les message propagé par l'utilisateur (./message?type=SPREADED)
		else if (MessageType.SPREADED.name().equals(type)) {
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
		//TODO
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
	 * Liste les messages de l'utilisateur donné (./users/$userId/messages)
	 */
	def indexUserMsg() {
		def userId = params.userId
		respond Message.where { author.id == userId }.list()
	}

	/**
	 * Liste les messages en attente de décisoin de propagation de l'utilisateur donné (./users/$userId/messages/toSent)
	 */
	def indexUserMsgToSent() {
		def userId = params.userId
		respond Message.createCriteria().list {
			sentTo{ eq('id', userId.toLong()) }
		}
	}

	/**
	 * Liste les messages propagé par l'utilisateur donné (./users/$userId/messages/spreaded)
	 */
	def indexUserMsgSpreaded() {
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
		// TODO
		render "Au boulot"
	}

	@Override
	public Object create() {
		// Non nécessaire pour le moment
	}

	@Override
	public Object edit() {
		// Non nécessaire pour le moment
	}

	@Override
	public Object patch() {
		// Non nécessaire pour le moment
	}
}
