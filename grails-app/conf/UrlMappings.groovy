class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

		"/"(view:"/index")
		"500"(view:"/error")

		// Déclaration des URL d'accès aux messages
		"/rest/messages"(resources:'message', excludes:["create", "edit", "patch"]) { // TODO exclure aussi le index
			"/spread"(controller:"message", action:"spread", method:"POST")
			"/ignore"(controller:"message", action:"ignore", method:"POST")
			"/report"(controller:"message", action:"report", method:"POST")
		}
		"/rest/users/$userId/messages"(controller:"message", action:"index", method:"GET")

		// Déclaration des URL d'accès aux utilisateurs
		"/rest/users"(resources:"user", excludes:["create", "edit", "patch"])

	}
}
