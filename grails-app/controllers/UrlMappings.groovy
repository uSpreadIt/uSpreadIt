class UrlMappings {

    static mappings = {
        // See https://grails.github.io/grails-doc/latest/guide/single.html#urlmappings
        // Cohérence de nommage Grails à respecter : exemple pour une déclaration d'urlmapping comme ceci : "/books"(resources:'book') signifie ceci :
        // HTTP Method   URI                  Grails Action
        // GET           /books               index
        // GET           /books/create        create
        // POST          /books               save
        // GET           /books/${id}         show
        // GET           /books/${id}/edit    edit
        // PUT           /books/${id}         update
        // DELETE        /books/${id}         delete
        //
        // Donc on utilisera le prefixe show pour le nommage de l'action pour le cas de retourner un élément. Et index pour retourner plusieurs éléments

        "/"(view: '/application/index')
        "500"(view: '/application/serverError')
        "404"(view: '/application/notFound')

        // API Rest
        group("/rest", {
            // Déclaration des URL d'interaction avec les messages
            "/messages"(resources:'message', excludes:["create", "edit", "patch", "update"]) {
                "/spread"(controller:"message", action:"spread", method:"POST")
                "/ignore"(controller:"message", action:"ignore", method:"POST")
                "/report"(controller:"message", action:"report", method:"POST")
            }

            "/users/$userId/messages"(controller:"message", action:"indexUserMsg", method:"GET")
            "/users/$userId/messages/received"(controller:"message", action:"indexUserMsgReceived", method:"GET")
            "/users/$userId/messages/spread"(controller:"message", action:"indexUserMsgSpread", method:"GET")
            "/messages/reported"(controller:"message", action:"indexMsgReported", method:"GET")

            // Déclaration des URL d'interaction avec les utilisateurs
            "/signup"(controller:"user", action:"save", method:"POST")
            "/login"(controller:"user", action:"login", method:"POST")
            "/users/connected"(controller:"user", action:"showUserConnected", method:"GET")
            "/users/connected"(controller:"user", action:"updateUserConnected", method:"PUT")
            "/users/connected"(controller:"user", action:"deleteUserConnected", method:"DELETE")
            "/users/connected/pushtoken"(controller:"user", action:"savePushToken", method:"POST")
            "/users/connected/status"(controller:"user", action:"showStatus", method:"GET")
			"/users/connected/password"(controller:"user", action:"changePassword", method:"PUT")

            "/users/moderator"(controller:"user", action:"saveModerator", method:"POST")
            "/users"(resources:"user", excludes:["create", "edit", "patch", "save"])
            "/users/topusers"(controller:"user", action:"indexTopUsers", method:"GET")
        })

    }
}
