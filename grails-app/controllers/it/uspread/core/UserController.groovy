package it.uspread.core

import grails.rest.RestfulController

class UserController extends RestfulController {

	static scope = "singleton"
	static responseFormats = ["json"]

	def springSecurityService

	UserController() {
		super(User)
	}

	/**
	 * Retourne les infos de l'utilisateur connecté
	 */
	def getUserConnected() {
		def user = (User) springSecurityService.currentUser
		respond User.where { id == user.id }.find()
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
