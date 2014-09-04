package it.uspread.core

import grails.rest.RestfulController

class MessageController extends RestfulController {

	static scope = "singleton"
    static responseFormats = ["json"]

    MessageController() {
        super(Message)
    }

	def spread() {

	}

	def ignore() {

	}

	def report() {

	}
}
