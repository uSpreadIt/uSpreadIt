package it.uspread.core

import grails.rest.RestfulController

class MessageController extends RestfulController {

    static responseFormats = ["json"]

    MessageController(){
        super(Message)
    }
}
