package it.uspread.core

import grails.rest.RestfulController

class UserController extends RestfulController {

	static scope = "singleton"
	static responseFormats = ["json"]

	UserController() {
		super(User)
	}
}
