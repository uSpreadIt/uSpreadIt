package it.uspread.core

import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.Transactional
import it.uspread.core.marshallers.JSONMarshaller

import org.springframework.http.HttpStatus

/**
 * Controlleur des accés aux utilisateur
 */
class UserController extends RestfulController<User> {

    static scope = "singleton"
    static responseFormats = ["json"]
    // TODO à paramétrer
    private static final int TOP_SIZE = 50;

    def springSecurityService

    UserController() {
        super(User)
    }

    /**
     * Retourne les infos de l'utilisateur connecté
     */
    def getUserConnected() {
        def user = (User) springSecurityService.currentUser
        if (null != user){
            JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
                respond User.where { id == user.id }.find()
            }
        }
        else {
            redirect(uri: '/')
        }
    }

    /**
     * Supprime le compte de l'user connecté
     */
    @Transactional
    def deleteUserConnected(){
        def instance = (User) springSecurityService.currentUser
        if (null != instance){
            deleteUser(instance)
        }
        request.withFormat {
            '*'{ render status: HttpStatus.NO_CONTENT } // NO CONTENT STATUS CODE
        }
    }

    private deleteUser(instance) {
        Message.createCriteria().list {
            sentTo {
                eq('id', instance.id)
            }
        }.each { ((Message) it).removeFromSentTo(instance) }
        Message.createCriteria().list {
            ignoredBy {
                eq('id', instance.id)
            }
        }.each { ((Message) it).removeFromIgnoredBy(instance) }
        Message.createCriteria().list {
            reports {
                eq('reporter.id', instance.id)
            }
        }.each {
            ((Message)it).removeFromReports(new Report(instance))
        }
        Report.createCriteria().list {
            reporter {
                eq('id', instance.id)
            }
        }.each{ ((Report) it).delete(flush: true) }
        Message.createCriteria().list {
            spreadBy {
                eq('id', instance.id)
            }
        }.each { ((Message) it).removeFromSpreadBy(instance) }
        instance.delete(flush: true)
    }

    /**
     * Update du compte de l'user connecté
     */
    @Transactional
    def updateUserConnected(){
        def instance = (User) springSecurityService.currentUser

        instance.properties = getObjectToBind()

        if (instance.hasErrors()) {
            respond instance.errors // STATUS CODE 422
            return
        }

        instance.save flush:true
        request.withFormat {
            '*'{
                render([status: HttpStatus.OK])
            }
        }
    }

    @Override
    def index(Integer max) {
        def user = (User) springSecurityService.currentUser
        params.max = Math.min(max ?: 10, 100)
        JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
            respond listAllResources(params), model: [("${resourceName}Count".toString()): countResources()]
        }
    }

    @Override
    def create() {
        // Non nécessaire car méthode d'obtention de formulaire de création.
    }

    @Override
    def edit() {
        // Non nécessaire car méthode d'obtention de formulaire d'édition.
    }

    @Override
    def patch() {
        // Non nécessaire pour le moment
    }

    @Override
    def save() {
        if(handleReadOnly()) {
            return
        }
        User instance = createResource()
        instance.clearForCreation()
        instance.validate()
        if (instance.hasErrors()) {
            respond instance.errors, view:'create' // STATUS CODE 422
            return
        }

        instance.save flush:true

        request.withFormat {
            '*' {
                render([status: HttpStatus.CREATED])
            }
        }
    }

    @Override
    def show() {
        def user = (User) springSecurityService.currentUser
        JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL_MARSHALLER : JSONMarshaller.PUBLIC_MARSHALLER) {
            respond queryForResource(params.id)
        }
    }

    @Override
    def update() {
        if(handleReadOnly()) {
            return
        }

        User instance = queryForResource(params.id)
        if (instance == null) {
            notFound()
            return
        }

        instance.properties = getObjectToBind()

        if (instance.hasErrors()) {
            respond instance.errors // STATUS CODE 422
            return
        }

        instance.save flush:true
        request.withFormat {
            '*'{
                render([status: HttpStatus.OK])
            }
        }
    }

    @Override
    def delete() {
        if(handleReadOnly()) {
            return
        }

        def instance = queryForResource(params.id)
        if (instance == null) {
            notFound()
            return
        }

        deleteUser(instance)

        request.withFormat {
            "*"{ render([status: HttpStatus.NO_CONTENT]) } // NO CONTENT STATUS CODE
        }
    }

    def topUsers() {
        JSON.use(JSONMarshaller.PUBLIC_MARSHALLER) {
            respond User.list([max: TOP_SIZE, sort: 'score', order: 'desc'])
        }
    }
}
