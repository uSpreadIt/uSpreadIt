import it.uspread.core.Message

class BootStrap {

    def init = { servletContext ->
		new Message(message:"Test").save()
    }
    def destroy = {
    }
}
