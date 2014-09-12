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
		// TODO
		render "Au boulot"
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
}
