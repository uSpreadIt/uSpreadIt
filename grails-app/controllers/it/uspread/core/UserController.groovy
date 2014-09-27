package it.uspread.core

import grails.rest.RestfulController
import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.servlet.HttpHeaders

import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

class UserController extends RestfulController<User> {

	static scope = "singleton"
	static responseFormats = ["json"]

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
            respond User.where { id == user.id }.find()
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
            instance.delete(flush: true)
        }
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: "${resourceClassName}.label".toString(), default: resourceClassName), instance.id])
                redirect(uri: '/')
            }
            '*'{ render status: NO_CONTENT } // NO CONTENT STATUS CODE
        }
    }

    /**
     * Update du compte de l'user connecté
     */
    @Transactional
    def updateUserConnected(){
        def instance = (User) springSecurityService.currentUser

        instance.properties = getObjectToBind()

        if (instance.hasErrors()) {
            respond instance.errors, view:'edit' // STATUS CODE 422
            return
        }

        instance.save flush:true
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: "${resourceClassName}.label".toString(), default: resourceClassName), instance.id])
                redirect instance
            }
            '*'{
                response.addHeader(HttpHeaders.LOCATION,
                        g.createLink(
                                resource: this.controllerName, action: 'show',id: instance.id, absolute: true,
                                namespace: hasProperty('namespace') ? this.namespace : null ))
                respond instance, [status: OK]
            }
        }
    }

    @Override
    def index(Integer max) {
        return super.index(max)
    }

    @Override
	def create() {
		// Non nécessaire pour le moment
	}

	@Override
	def edit() {
		// Non nécessaire pour le moment
	}

	@Override
	def patch() {
		// Non nécessaire pour le moment
	}

    @Override
    def save() {
        return super.save()
    }

    @Override
    def show() {
        return super.show()
    }

    @Override
    def update() {
        return super.update()
    }

    @Override
    def delete() {
        return super.delete()
    }
}
