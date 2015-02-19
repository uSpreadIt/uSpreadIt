package it.uspread.core

import grails.converters.JSON
import grails.rest.RestfulController
import it.uspread.core.json.JSONAttribute
import it.uspread.core.json.JSONMarshaller
import it.uspread.core.params.QueryParams

import org.springframework.http.HttpStatus

/**
 * Controlleur des accés aux utilisateurs
 */
class UserController extends RestfulController<User> {

    static scope = "singleton"
    static responseFormats = ["json"]

    def springSecurityService
    def userService
    def messageService
    def androidGcmPushService

    UserController() {
        super(User)
    }

    def login() {
        // FIXME le code changera quand on aura un système de login : pour le moment pas de login donc l'user connecté viens des credential envoyé a chaque requete
        def user = (User) springSecurityService.currentUser
        if (null != user){
            if (!user.isModerator()) {
                String device = params.device;
                String pushToken = params.pushToken
                if (QueryParams.DEVICE_ANDROID.equals(device) && pushToken != null) {
                    androidGcmPushService.reservePushTokenToUser(user, pushToken)
                } else if (QueryParams.DEVICE_IOS.equals(device) && pushToken != null) {
                    // TODO
                }
                render([status: HttpStatus.OK])
            }
        }
        else {
            forbidden()
        }
    }

    /**
     * Retourne les infos de l'utilisateur connecté
     */
    def getUserConnected() {
        def user = (User) springSecurityService.currentUser
        if (null != user){
            JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL : JSONMarshaller.PUBLIC_USER) {
                respond(userService.getUserFromId(user.id))
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
        render([status: HttpStatus.NO_CONTENT])
    }

    /**
     * Update du compte de l'user connecté
     */
    def updateUserConnected(){
        def instance = (User) springSecurityService.currentUser

        updateFromRequest(instance)

        if (instance.hasErrors()) {
            respond(instance.errors) // STATUS CODE 422
            return
        }

        instance.save flush:true
        render([status: HttpStatus.OK])
    }

    @Override
    def index(Integer max) {
        def user = (User) springSecurityService.currentUser
        params.max = Math.min(max ?: 10, 100)
        JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL : JSONMarshaller.PUBLIC_USER) {
            respond(listAllResources(params), model: [("${resourceName}Count".toString()): countResources()])
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
        instance.validate()
        if (instance.hasErrors()) {
            respond(instance.errors, view:'create') // STATUS CODE 422
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
        instance.validate()
        if (instance.hasErrors()) {
            respond(instance.errors, view:'create') // STATUS CODE 422
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
        JSON.use(user.isModerator() ? JSONMarshaller.INTERNAL : JSONMarshaller.PUBLIC_USER) {
            respond(queryForResource(params.id))
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

        updateFromRequest(instance)

        if (instance.hasErrors()) {
            respond(instance.errors) // STATUS CODE 422
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
            "*"{
                render([status: HttpStatus.NO_CONTENT])
            }
        }
    }

    /**
     * Retourne le status de l'utilisateur connecté
     * @return
     */
    def status() {
        def user = (User) springSecurityService.currentUser
        if (user.isModerator()) {
            return
        }

        // Lecture des paramètres
        boolean quotaOnly = params.quotaOnly != null ? new Boolean((String)params.quotaOnly).booleanValue() : false

        JSON.use(quotaOnly ? JSONMarshaller.PUBLIC_STATUS_QUOTA : JSONMarshaller.PUBLIC_STATUS) {
            respond(messageService.getUserMessagesStatus(user, quotaOnly))
        }
    }

    /**
     * Enregistrement d'un nouveau token PUSH pour l'utilisateur connecté
     * @return
     */
    def registerPushToken() {
        def user = (User) springSecurityService.currentUser
        if (null != user && !user.isModerator()) {
            String device = request.JSON.opt(JSONAttribute.USER_DEVICE) ?: null
            String pushToken = request.JSON.opt(JSONAttribute.USER_PUSHTOKEN) ?: null
            if (QueryParams.DEVICE_ANDROID.equals(device) && pushToken != null) {
                androidGcmPushService.registerPushToken(user, pushToken)
                render([status: HttpStatus.OK])
            } else if (QueryParams.DEVICE_IOS.equals(device) && pushToken != null) {
                // TODO
                render([status: HttpStatus.OK])
            }
            else {
                render([status: HttpStatus.BAD_REQUEST])
            }
        } else {
            forbidden()
        }
    }

    def topUsers() {
        JSON.use(JSONMarshaller.PUBLIC_USER_SCORE) {
            respond(userService.getTopUsers())
        }
    }

    def forbidden() {
        render([status: HttpStatus.FORBIDDEN])
    }

    @Override
    protected getObjectToBind() {
        User user = new User()
        updateFromRequest(user)
        return user
    }

    private void updateFromRequest(User user) {
        user.username = request.JSON.opt(JSONAttribute.USER_USERNAME) ?: user.username
        user.password = request.JSON.opt(JSONAttribute.USER_PASSWORD) ?: user.password
        user.email = request.JSON.opt(JSONAttribute.USER_EMAIL) ?: user.email
        user.iosPushToken = request.JSON.opt(JSONAttribute.USER_IOSPUSHTOKEN) ?: user.iosPushToken
    }
}
