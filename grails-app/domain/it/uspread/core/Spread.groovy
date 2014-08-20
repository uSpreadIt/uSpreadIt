package it.uspread.core

import grails.rest.Resource

@Resource(formats=['json'], superClass = SpreadController)
class Spread {

	String message;

    static constraints = {
    }
}
