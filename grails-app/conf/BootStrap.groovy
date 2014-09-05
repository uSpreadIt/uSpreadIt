import it.uspread.core.Message
import it.uspread.core.User

class BootStrap {

	def init = { servletContext ->
		// Vérifie si les données de test sont déjà présente
		if (!Message.count()) {
			def chuck = new User(username: 'chuck', password: 'chuck', email:"ChuckNoris@42.fr")
            def etienne = new User(username: 'etienne', password: 'etienne', email:"Etienne@free.fr")

            chuck.addToMessages(new Message(text:"Un message de chuck", sentTo:[etienne]))
            chuck.addToMessages(new Message(text:"Un autre de chuck", sentTo:[etienne]))
			chuck.save()

            etienne.addToMessages(new Message(text:"Un message de Etienne", sentTo:[chuck]))
            etienne.addToMessages(new Message(text:"Un autre de Etienne", sentTo:[chuck]))
			etienne.save()
		}
	}

	def destroy = {
	}
}
