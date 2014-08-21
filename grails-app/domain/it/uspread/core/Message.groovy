package it.uspread.core

import grails.rest.Resource

// le mapping uri doit etre dans UrlMappings.groovy en raison de la def des actions supplémentaire (semble impossible avec l'annotation)
@Resource(formats=['json'], superClass=MessageController)
class Message {

	String message;

	static constraints = {
	}
}
