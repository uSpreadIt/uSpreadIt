class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')

		// Déclaration des URL des messages
		"/rest/message"(resources:'message') {
			"/spread"(controller:"message", action:"spread", method: 'POST')
			"/ignore"(controller:"message", action:"ignore", method: 'POST')
			"/report"(controller:"message", action:"report", method: 'POST')
		}

		// Déclaration des URL des utilisateurs
		"/rest/user"(resources:'user') {
		}

	}
}
