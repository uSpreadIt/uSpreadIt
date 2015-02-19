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
        "/rest/messages"(resources:'message', excludes:["create", "edit", "patch", "update"]) {
            "/spread"(controller:"message", action:"spread", method:"POST")
            "/ignore"(controller:"message", action:"ignore", method:"POST")
            "/report"(controller:"message", action:"report", method:"POST")
        }

        "/rest/users/$userId/messages"(controller:"message", action:"indexUserMsg", method:"GET")
        "/rest/users/$userId/messages/received"(controller:"message", action:"indexUserMsgReceived", method:"GET")
        "/rest/users/$userId/messages/spread"(controller:"message", action:"indexUserMsgSpread", method:"GET")
        "/rest/messages/reported"(controller:"message", action:"indexMsgReported", method:"GET")

        // Déclaration des URL d'accès aux utilisateurs
        "/rest/signup"(controller:"user", action:"save", method:"POST")
        "/rest/login"(controller:"user", action:"login", method:"POST")
        "/rest/users/connected"(controller:"user", action:"getUserConnected", method:"GET")
        "/rest/users/connected"(controller:"user", action:"updateUserConnected", method:"PUT")
        "/rest/users/connected"(controller:"user", action:"deleteUserConnected", method:"DELETE")
        "/rest/users/connected/pushtoken"(controller:"user", action:"registerPushToken", method:"POST")
        "/rest/users/connected/status"(controller:"user", action:"status", method:"GET")

        "/rest/users/moderator"(controller:"user", action:"createModerator", method:"POST")
        "/rest/userlist"(controller:"user", action:"index", method:"GET")
        "/rest/users"(resources:"user", excludes:["create", "edit", "patch", "save", "index"])
        "/rest/topusers"(controller:"user", action:"topUsers", method:"GET")

    }
}
