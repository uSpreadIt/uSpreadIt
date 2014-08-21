package it.uspread.core

import grails.rest.RestfulController

class MessageController extends RestfulController<Message> {

	MessageController() {
		super(Message)
	}

}
