import it.uspread.core.Message
import it.uspread.core.User

class BootStrap {

	def init = { servletContext ->
		// Vérifie si les données de test sont déjà présente
		if (!Message.count()) {
			def user1 = new User(email:"ChuckNoris@42.fr")
			user1.save()
			new Message(text:"Un message de chuck", author:user1, nbSpread:1).save()
			new Message(text:"Un autre de chuck", author:user1, nbSpread:1).save()

			def user2 = new User(email:"Etienne@free.fr")
			user2.save()
			new Message(text:"Un message de Etienne", author:user2, nbSpread:1).save()
			new Message(text:"Un autre de Etienne", author:user2, nbSpread:1).save()
		}
	}

	def destroy = {
	}
}
