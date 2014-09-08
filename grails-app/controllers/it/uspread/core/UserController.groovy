package it.uspread.core

import grails.rest.RestfulController

class UserController extends RestfulController {

	static scope = "singleton"
	static responseFormats = ["json"]

	UserController() {
		super(User)
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
