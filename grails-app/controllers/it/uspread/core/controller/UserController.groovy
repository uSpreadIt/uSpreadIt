package it.uspread.core.controller

import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.Transactional
import it.uspread.core.domain.User
import it.uspread.core.json.JSONAttribute
import it.uspread.core.json.JSONMarshaller
import it.uspread.core.params.QueryParams

import org.springframework.http.HttpStatus

/**
 * Controlleur des accés aux utilisateurs
 */
class UserController extends RestfulController<User> {

    static scope = 'singleton'
    static responseFormats = ['json']

    def springSecurityService
    def userService
    def messageService
    def roleService
    def androidGcmService
    def iosAPNSService

    UserController() {
        super(User)
    }

    /**
     * Liste les utilisateurs
     */
    @Override
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)

        // TODO prendre en compte les critères URL et la réponse BAD_REQUEST  (et faire une classe UserCriteria)

        JSON.use(JSONMarshaller.INTERNAL) {
            return respond(listAllResources(params), [status: HttpStatus.OK])
        }
    }

    @Override
    def show() {
        User user = queryForResource(params.id)
        if (user == null) {
            return renderNotFound()
        }

        JSON.use(JSONMarshaller.INTERNAL) {
            return respond(user, [status: HttpStatus.OK])
        }
    }

    @Override
    def create() {
        // Non nécessaire car méthode d'obtention de formulaire de création.
    }

    @Transactional
    @Override
    def save() {
        User newUser = createResource()
        newUser.validate()
        if (newUser.hasErrors()) {
            return renderBadRequest()
        }

        newUser.save([flush:true])
        roleService.setRoleUser(newUser)

        return render([status: HttpStatus.CREATED])
    }

    @Override
    def edit() {
        // Non nécessaire car méthode d'obtention de formulaire d'édition.
    }

    @Transactional
    @Override
    def patch() {
        // Non nécessaire pour le moment
    }

    @Transactional
    @Override
    def update() {
        User user = queryForResource(params.id)
        if (user == null) {
            return renderNotFound()
        }

        updateFromRequest(user)
        user.validate()
        if (user.hasErrors()) {
            return renderBadRequest()
        }

        user.save([flush:true])

        return render([status: HttpStatus.ACCEPTED])
    }

    @Transactional
    @Override
    def delete() {
        def user = queryForResource(params.id)
        if (user == null) {
            return renderNotFound()
        }

        userService.deleteUser(user)

        return render([status: HttpStatus.ACCEPTED])
    }

    def login() {
        // FIXME le code changera quand on aura fixé le système de login
        def userConnected = (User) springSecurityService.currentUser
        if (null != userConnected){
            if (!userConnected.isSpecialUser()) {
                String device = params.device;
                String pushToken = params.pushToken
                if (QueryParams.DEVICE_ANDROID == device && pushToken != null) {
                    androidGcmService.reservePushTokenToUser(userConnected, pushToken)
                } else if (QueryParams.DEVICE_IOS == device && pushToken != null) {
                    iosAPNSService.reservePushTokenToUser(userConnected, pushToken)
                }
                return render([status: HttpStatus.ACCEPTED])
            } else {
                return render([status: HttpStatus.ACCEPTED])
            }
        }
        else {
            return renderForbidden()
        }
    }

    /**
     * Retourne les infos de l'utilisateur connecté
     */
    def showUserConnected() {
        def userConnected = (User) springSecurityService.currentUser
        JSON.use(userConnected.isSpecialUser() ? JSONMarshaller.INTERNAL : JSONMarshaller.PUBLIC_USER) {
            return respond(userService.getUserFromId(userConnected.id), [status: HttpStatus.OK])
        }
    }

    /**
     * Update du compte de l'user connecté
     */
    @Transactional
    def updateUserConnected(){
        def userConnected = (User) springSecurityService.currentUser

        updateFromRequest(userConnected)

        userConnected.validate()
        if (userConnected.hasErrors()) {
            return renderBadRequest()
        }

        userConnected.save([flush:true])
        return render([status: HttpStatus.ACCEPTED])
    }

    /**
     * Supprime le compte de l'user connecté
     */
    @Transactional
    def deleteUserConnected(){
        def userConnected = (User) springSecurityService.currentUser
        userService.deleteUser(userConnected)
        return render([status: HttpStatus.ACCEPTED])
    }

    /**
     * Création d'un modérateur
     * @return
     */
    @Transactional
    def saveModerator() {
        User newModerator = createResource()
        newModerator.specialUser = true
        newModerator.validate()
        if (newModerator.hasErrors()) {
            return renderBadRequest()
        }

        newModerator.save([flush:true])
        roleService.setRoleModerator(newModerator)

        return render([status: HttpStatus.CREATED])
    }

    /**
     * Retourne le status de l'utilisateur connecté
     * @return
     */
    def showStatus() {
        def user = (User) springSecurityService.currentUser

        // Lecture des paramètres
        boolean quotaOnly = params.quotaOnly != null ? new Boolean((String)params.quotaOnly).booleanValue() : false

        JSON.use(quotaOnly ? JSONMarshaller.PUBLIC_STATUS_QUOTA : JSONMarshaller.PUBLIC_STATUS) {
            return respond(messageService.getUserMessagesStatus(user, quotaOnly), [status: HttpStatus.OK])
        }
    }

    /**
     * Enregistrement d'un nouveau token PUSH pour l'utilisateur connecté
     * @return
     */
    @Transactional
    def savePushToken() {
        def user = (User) springSecurityService.currentUser
        String device = request.JSON.opt(JSONAttribute.USER_DEVICE) ?: null
        String pushToken = request.JSON.opt(JSONAttribute.USER_PUSHTOKEN) ?: null
        if (QueryParams.DEVICE_ANDROID == device && pushToken != null) {
            androidGcmService.registerPushToken(user, pushToken)
            return render([status: HttpStatus.ACCEPTED])
        } else if (QueryParams.DEVICE_IOS == device && pushToken != null) {
            iosAPNSService.registerPushToken(user, pushToken)
            return render([status: HttpStatus.ACCEPTED])
        }
        else {
            return renderBadRequest()
        }
    }

    def indexTopUsers() {
        JSON.use(JSONMarshaller.PUBLIC_USER_SCORE) {
            return respond(userService.getTopUsers(), [status: HttpStatus.OK])
        }
    }

    private def renderBadRequest() {
        return render([status: HttpStatus.BAD_REQUEST])
    }

    private def renderNotFound() {
        return render([status: HttpStatus.NOT_FOUND])
    }

    private def renderForbidden() {
        return render([status: HttpStatus.FORBIDDEN])
    }

    @Override
    protected getObjectToBind() {
        User user = new User()
        updateFromRequest(user)
        return user
    }

    /**
     * Renseigne l'utilisateur a partir des donnée fournie
     * @param user
     */
    private void updateFromRequest(User user) {
        user.username = request.JSON.opt(JSONAttribute.USER_USERNAME) ?: user.username
        user.password = request.JSON.opt(JSONAttribute.USER_PASSWORD) ?: user.password
        user.email = request.JSON.opt(JSONAttribute.USER_EMAIL) ?: user.email
    }
}
