package it.uspread.core.controller

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.RestfulController
import grails.transaction.Transactional
import it.uspread.core.domain.User
import it.uspread.core.json.JSONAttribute
import it.uspread.core.json.JSONMarshaller
import it.uspread.core.params.URLParamsName
import it.uspread.core.params.URLParamsValue
import it.uspread.core.service.MessageService
import it.uspread.core.service.RoleService
import it.uspread.core.service.UserService
import it.uspread.core.service.android.AndroidGcmService
import it.uspread.core.service.ios.IosAPNSService

import org.springframework.http.HttpStatus

/**
 * Controlleur des accés aux utilisateurs
 */
class UserController extends RestfulController<User> {

    static scope = 'singleton'
    static responseFormats = ['json']

    SpringSecurityService springSecurityService

    UserService userService
    MessageService messageService
    RoleService roleService
    AndroidGcmService androidGcmService
    IosAPNSService iosAPNSService

    UserController() {
        super(User)
    }

    /**
     * Liste les utilisateurs (Inaccessible niveau sécurité par les utilistateurs publics)
     */
    @Override
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)

        // TODO prendre en compte les critères URL et la réponse BAD_REQUEST  (et faire une classe UserCriteria)

        JSON.use(JSONMarshaller.INTERNAL) {
            return respond(listAllResources(params), [status: HttpStatus.OK])
        }
    }

    /**
     * Retourne les informations d'un utilisateur  (Inaccessible niveau sécurité par les utilistateurs publics)
     */
    @Override
    def show() {
        User user = queryForResource(params[URLParamsName.ID])
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
        // Vérification du quota
        if (userService.isLimitReached()) {
            return render([status: 551, text: 'User Quota reached'])
        }

        User newUser = createResource()
        newUser.publicUser = true
        newUser.validate()
        if (newUser.hasErrors()) {
            return renderBadRequest()
        }

        newUser.save([flush:true])
        roleService.setRolePublic(newUser)

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


    /**
     * Met à jour les informations d'un utilisateur  (Inaccessible niveau sécurité par les utilistateurs publics)
     */
    @Transactional
    @Override
    def update() {
        User user = queryForResource(params[URLParamsName.ID])
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
        def user = queryForResource(params[URLParamsName.ID])
        if (user == null) {
            return renderNotFound()
        }

        userService.deleteUser(user)

        return render([status: HttpStatus.ACCEPTED])
    }

    def login() {
        // FIXME le code changera quand on aura fixé le système de login
        User userConnected = (User) springSecurityService.currentUser
        if (null != userConnected){
            if (userConnected.publicUser) {
                String device = params[URLParamsName.USER_DEVICE];
                String pushToken = params[URLParamsName.USER_PUSHTOKEN]
                if (URLParamsValue.DEVICE_ANDROID == device && pushToken != null) {
                    androidGcmService.reservePushTokenToUser(userConnected, pushToken)
                } else if (URLParamsValue.DEVICE_IOS == device && pushToken != null) {
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
        User userConnected = (User) springSecurityService.currentUser
        JSON.use(userConnected.publicUser ? JSONMarshaller.PUBLIC_USER : JSONMarshaller.INTERNAL) {
            return respond(userService.getUserFromId(userConnected.id), [status: HttpStatus.OK])
        }
    }

    /**
     * Update du compte de l'user connecté
     */
    @Transactional
    def updateUserConnected(){
        User userConnected = (User) springSecurityService.currentUser

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
        User userConnected = (User) springSecurityService.currentUser
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
        newModerator.publicUser = false
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
        User user = (User) springSecurityService.currentUser

        // Lecture des paramètres
        boolean quotaOnly = params[URLParamsName.USER_ONLY_QUOTA] != null ? Boolean.parseBoolean((String)params[URLParamsName.USER_ONLY_QUOTA]) : false

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
        User user = (User) springSecurityService.currentUser
        String device = request.JSON.opt(JSONAttribute.USER_DEVICE) ?: null
        String pushToken = request.JSON.opt(JSONAttribute.USER_PUSHTOKEN) ?: null
        if (URLParamsValue.DEVICE_ANDROID == device && pushToken != null) {
            androidGcmService.registerPushToken(user, pushToken)
            return render([status: HttpStatus.ACCEPTED])
        } else if (URLParamsValue.DEVICE_IOS == device && pushToken != null) {
            iosAPNSService.registerPushToken(user, pushToken)
            return render([status: HttpStatus.ACCEPTED])
        }
        else {
            return renderBadRequest()
        }
    }
	
	/**
	 * Changement du mot de passe de l'utilisateur connecté
	 * @return
	 */
	@Transactional
	def changePassword() {
		User user = (User) springSecurityService.currentUser
		String oldPassword = request.JSON.opt(JSONAttribute.USER_OLD_PASSWORD)
		String newPassword = request.JSON.opt(JSONAttribute.USER_PASSWORD)
		if (springSecurityService.passwordEncoder.isPasswordValid(user.password, oldPassword, null)) {
			user.password = newPassword
			user.validate()
			if (user.hasErrors()) {
				return renderBadRequest()
			}
			return render([status: HttpStatus.ACCEPTED])
		}
		else {
			return renderForbidden()
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
     * Renseigne ou met à jour l'utilisateur a partir des données qui seraient fournie dans le JSON (Ne touche pas ce qui n'est pas fourni)
     * @param user
     */
    private void updateFromRequest(User user) {
        user.username = request.JSON.opt(JSONAttribute.USER_USERNAME) ?: user.username
        user.password = request.JSON.opt(JSONAttribute.USER_PASSWORD) ?: user.password
        user.email = request.JSON.opt(JSONAttribute.USER_EMAIL) ?: user.email
        user.preferredLanguage = request.JSON.opt(JSONAttribute.USER_PREFLANGUAGE) ?: user.preferredLanguage
        if (request.JSON.has(JSONAttribute.USER_LOCATION)) {
            user.location = request.JSON.get(JSONAttribute.USER_LOCATION)
            if (user.location?.trim().isEmpty()) {
                user.location = null
            }
            user.located = user.location != null
        }
        user.messageLocated = request.JSON.opt(JSONAttribute.USER_MESSAGELOCATED) ?: user.messageLocated
    }
}
