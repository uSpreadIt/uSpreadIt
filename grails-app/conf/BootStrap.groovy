import it.uspread.core.Message
import it.uspread.core.User

class BootStrap {

	def init = { servletContext ->
		// Vérifie si les données de test sont déjà présente
		if (!Message.count()) {
			def user1 = new User(username: 'chuck', password: 'chuck', email:"ChuckNoris@42.fr")
            user1.addToMessages(new Message(text:"Un message de chuck", nbSpread:1))
            user1.addToMessages(new Message(text:"Un autre de chuck", nbSpread:1))
			user1.save()

			def user2 = new User(username: 'etienne', password: 'etienne', email:"Etienne@free.fr")
            user2.addToMessages(new Message(text:"Un message de Etienne", nbSpread:1))
            user2.addToMessages(new Message(text:"Un autre de Etienne", nbSpread:1))
			user2.save()
		}
	}

	def destroy = {
	}
}
