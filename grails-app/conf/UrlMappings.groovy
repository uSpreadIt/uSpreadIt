class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')

		"/rest/v1.0/spread"(resources:"spread") {
			//TODO
			"/diffuse"(controller:"diffuse", method:"GET")
			"/moderate"(controller:"moderate", method:"DELETE")
		}
	}
}
