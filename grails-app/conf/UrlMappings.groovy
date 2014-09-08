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
		"/rest/messages"(resources:'message', excludes:["create", "edit", "patch"]) {
			"/spread"(controller:"message", action:"spread", method:"POST")
			"/ignore"(controller:"message", action:"ignore", method:"POST")
			"/report"(controller:"message", action:"report", method:"POST")
		}

		"/rest/users/$userId/messages"(controller:"message", action:"indexUserMsg", method:"GET")
		"/rest/users/$userId/messages/toSent"(controller:"message", action:"indexUserMsgToSent", method:"GET")
		"/rest/users/$userId/messages/spreaded"(controller:"message", action:"indexUserMsgSpreaded", method:"GET")
		"/rest/messages/reported"(controller:"message", action:"indexMsgReported", method:"GET")

		// Déclaration des URL d'accès aux utilisateurs
		"/rest/users"(resources:"user", excludes:["create", "edit", "patch"])

	}
}
