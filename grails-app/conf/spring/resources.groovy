import grails.rest.render.json.JsonCollectionRenderer
import grails.rest.render.json.JsonRenderer
import it.uspread.core.Message
import it.uspread.core.User

// Place your Spring DSL code here
beans = {

	// Configuration du rendu JSON des objets du domaine
	messageRenderer(JsonRenderer, Message) {
		excludes = ['class']
	}

	messageCollectionRenderer(JsonCollectionRenderer, Message) {
		excludes = ['class']
	}

	userRenderer(JsonRenderer, User) {
		excludes = ['class']
	}

	userCollectionRenderer(JsonCollectionRenderer, User) {
		excludes = ['class']
	}
}
