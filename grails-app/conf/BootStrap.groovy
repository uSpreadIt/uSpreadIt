import it.uspread.core.Spread

class BootStrap {

    def init = { servletContext ->
		new Spread(message:"Test").save()
    }
    def destroy = {
    }
}
