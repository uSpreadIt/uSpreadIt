package it.uspread.core

import grails.converters.JSON
import grails.rest.RestfulController
import it.uspread.core.marshallers.JSONMarshaller

import org.springframework.http.HttpStatus

/**
 * Controlleur des accés aux utilisateur
 */
class UserController extends RestfulController<User> {

    static scope = "singleton"
    static responseFormats = ["json"]

    def springSecurityService
    def userService
    def messageService

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
                respond userService.getUserFromId(user.id)
            }
        }
        else {
            redirect(uri: '/')
        }
    }

    /**
     * Supprime le compte de l'user connecté
     */
    def deleteUserConnected(){
        def instance = (User) springSecurityService.currentUser
        if (null != instance){
            userService.deleteUser(instance)
        }
        request.withFormat {
            '*'{ render status: HttpStatus.NO_CONTENT } // NO CONTENT STATUS CODE
        }
    }

    /**
     * Update du compte de l'user connecté
     */
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

    def createModerator() {
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

        def roleMod = new Role(authority: Role.ROLE_MODERATOR)
        roleMod.save()
        UserRole droitsMod = new UserRole(user: instance, role: roleMod)

        request.withFormat {
            '*' {
                render([status: HttpStatus.CREATED])
            }
        }
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

        userService.deleteUser(instance)

        request.withFormat {
            "*"{ render([status: HttpStatus.NO_CONTENT]) } // NO CONTENT STATUS CODE
        }
    }

    /**
     * Pour l'utilisateur connecté indique des choses : pour le moment si le quota de message est atteint
     * @return
     */
    def status() {
        def user = (User) springSecurityService.currentUser
        if (user.isModerator()) {
            return
        }
        render('{"quotaReached":"' + messageService.isMessageCreationLimitReached(user) + '"}', contentType: "application/json", encoding: "UTF-8")
    }

    def topUsers() {
        JSON.use(JSONMarshaller.PUBLIC_MARSHALLER) {
            respond userService.getTopUsers()
        }
    }
}
