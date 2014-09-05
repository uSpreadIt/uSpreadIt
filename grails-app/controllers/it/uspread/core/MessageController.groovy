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
		def userId = params.userId
		def sentToUser = params.sentTo
		def spreadByUser = params.spreadBy
		// Si on liste les messages d'un utilisateur particulier (./user/$id/message)
		if (userId) {
			respond Message.where { author.id == userId }.list()
		}
		// Si on liste les message en attente de décision de propagation pour l'utilisateur (./message?sentTo=user)
		else if (sentToUser) {
			respond Message.createCriteria().list {
				sentTo{ eq('id', sentToUser.toLong()) }
			}
		}
		// Si on liste les message propagé par l'utilisateur (./message?spreadBy=user)
		else if (spreadByUser) {
			respond Message.createCriteria().list {
				spreadBy{ eq('id', spreadByUser.toLong()) }
			}
		}
		// Sinon cas normal d'utilisation de l'index (./message)
		else {
            def user = (User) springSecurityService.currentUser
            respond Message.where { author.id == user.id }.list()
		}
	}

	/**
	 * Propage le message dont l'id est fourni lors de l'appel
	 */
	def spread() {
		def messageId = params.messageId
		//TODO notion d'utilisateur de cette action et lancement de l'action
	}

	/**
	 * Ignore le message dont l'id est fourni lors de l'appel
	 */
	def ignore() {
		def messageId = params.messageId
		//TODO notion d'utilisateur de cette action et lancement de l'action
	}

	/**
	 * Signale le message dont l'id est fourni lors de l'appel
	 */
	def report() {
		def messageId = params.messageId
		//TODO notion d'utilisateur de cette action et lancement de l'action
	}
}
