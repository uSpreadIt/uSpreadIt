package it.uspread.core

import grails.rest.RestfulController
import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.servlet.HttpHeaders

import static org.springframework.http.HttpStatus.CREATED
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
            deleteUser(instance)
        }
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: "${resourceClassName}.label".toString(), default: resourceClassName), instance.id])
                redirect(uri: '/')
            }
            '*'{ render status: NO_CONTENT } // NO CONTENT STATUS CODE
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
            reportedBy {
                eq('id', instance.id)
            }
        }.each { ((Message) it).removeFromReportedBy(instance) }
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
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: "${resourceName}.label".toString(), default: resourceClassName), instance.id])
                redirect instance
            }
            '*' {
                response.addHeader(HttpHeaders.LOCATION,
                        g.createLink(
                                resource: this.controllerName, action: 'show',id: instance.id, absolute: true,
                                namespace: hasProperty('namespace') ? this.namespace : null ))
                respond instance, [status: CREATED]
            }
        }
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
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: "${resourceClassName}.label".toString(), default: resourceClassName), instance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT } // NO CONTENT STATUS CODE
        }
    }
}
