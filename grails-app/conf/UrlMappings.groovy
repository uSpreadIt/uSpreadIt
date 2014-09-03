class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')

		/*"/rest/v1.0/message"(resources:"message") {
			//TODO
			"/spread"(controller:"spread", method:"GET")
			"/report"(controller:"report", method:"DELETE")
		}*/
	}
}
