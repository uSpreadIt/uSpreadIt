package it.uspread.core

import grails.rest.Resource


@Resource(uri='/rest/message', formats=['json'])
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
