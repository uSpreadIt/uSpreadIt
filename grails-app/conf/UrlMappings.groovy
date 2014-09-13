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
			"/spread"(controller:"message", action:"spread", method:"PUT")
			"/ignore"(controller:"message", action:"ignore", method:"PUT")
			"/report"(controller:"message", action:"report", method:"PUT")
		}

		"/rest/users/$userId/messages"(controller:"message", action:"indexUserMsg", method:"GET")
		"/rest/users/$userId/messages/received"(controller:"message", action:"indexUserMsgReceived", method:"GET")
		"/rest/users/$userId/messages/spread"(controller:"message", action:"indexUserMsgSpread", method:"GET")
		"/rest/messages/reported"(controller:"message", action:"indexMsgReported", method:"GET")

		// Déclaration des URL d'accès aux utilisateurs
		"/rest/users"(resources:"user", excludes:["create", "edit", "patch"])
		"/rest/users/connected"(controller:"user", action:"getUserConnected", method:"GET")

	}
}
