import it.uspread.core.Message
import it.uspread.core.User

class BootStrap {

	def init = { servletContext ->
		// Vérifie si les données de test sont déjà présente
		if (!Message.count()) {
			def chuck = new User(username: 'chuck', password: 'chuck', email:"ChuckNoris@42.fr")
			chuck.save()
			def etienne = new User(username: 'etienne', password: 'etienne', email:"Etienne@free.fr")
			etienne.save()

			new Message(text:"Un message de chuck", author:chuck, nbSpread:1, spreadBy:[chuck], sentTo:[etienne]).save()
			new Message(text:"Un autre de chuck", author:chuck, nbSpread:1, spreadBy:[chuck], sentTo:[etienne]).save()

			new Message(text:"Un message de Etienne", author:etienne, nbSpread:1, spreadBy:[etienne], sentTo:[chuck]).save()
			new Message(text:"Un autre de Etienne", author:etienne, nbSpread:1, spreadBy:[etienne], sentTo:[chuck]).save()
		}
	}

	def destroy = {
	}
}
