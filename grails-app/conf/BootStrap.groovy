import it.uspread.core.Message
import it.uspread.core.User

class BootStrap {

	def init = { servletContext ->
		// Vérifie si les données de test sont déjà présente
		if (!Message.count()) {
			def user1 = new User(email:"user1@wis.fr")
			user1.save()
			new Message(text:"Un message", author:user1, nbSpread:1).save()
			new Message(text:"Un autre", author:user1, nbSpread:1).save()
		}
	}

	def destroy = {
	}
}
