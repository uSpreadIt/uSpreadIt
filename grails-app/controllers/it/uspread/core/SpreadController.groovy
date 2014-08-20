package it.uspread.core

import grails.rest.RestfulController

class SpreadController extends RestfulController<Spread> {

	SpreadController() {
		super(Spread)
	}

    def index() { }
}
