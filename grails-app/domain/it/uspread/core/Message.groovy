package it.uspread.core

import grails.rest.Resource


// le mapping uri doit etre dans UrlMappings.groovy en raison de la def des actions supplémentaire (semble impossible avec l'annotation)
@Resource(formats=['json'], superClass=MessageController)
class Message {

	String text
    User author
	long nbSpread

    static belongsTo = User
    static hasMany = [sentTo: User, spreadBy: User, reportedBy: User]

	static constraints = {
        author(nullable: false)
	}
}
