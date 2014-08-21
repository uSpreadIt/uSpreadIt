import it.uspread.core.Message

class BootStrap {

    def init = { servletContext ->
		new Message(text:"Test").save()
    }
    def destroy = {
    }
}
