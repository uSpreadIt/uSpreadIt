import it.uspread.core.Message
import it.uspread.core.Role
import it.uspread.core.User
import it.uspread.core.UserRole

class BootStrap {

	def init = { servletContext ->
		// Vérifie si les données de test sont déjà présente
		if (!Message.count()) {
			def chuck = new User(username: 'chuck', password: 'chuck', email:"ChuckNoris@42.fr")
            def etienne = new User(username: 'etienne', password: 'etienne', email:"Etienne@free.fr")
            def mod = new User(username: 'mod', password: 'mod', email:"mod@free.fr")

            chuck.addToMessages(new Message(text:"Un message de chuck transmis à etienne", sentTo:[etienne]))
            chuck.addToMessages(new Message(text:"Un autre de chuck propagé par etienne", spreadBy: [etienne]))
            chuck.addToMessages(new Message(text:"Un autre de chuck signalé par etienne", reportedBy: [etienne]))
            chuck.addToMessages(new Message(text:"Un autre de chuck (impossible)"))
			chuck.save()

            etienne.addToMessages(new Message(text:"Un message de Etienne transmis à chuck", sentTo:[chuck]))
            etienne.addToMessages(new Message(text:"Un autre de Etienne propagé par chuck", spreadBy: [chuck]))
            etienne.addToMessages(new Message(text:"Un autre de Etienne signalé par chuck", reportedBy: [chuck]))
			etienne.save()


            def roleMod = new Role(authority: Role.ROLE_MODERATOR)
            roleMod.save()
            mod.save()
            UserRole droitsMod = new UserRole(user: mod, role: roleMod)
            droitsMod.save()
		}
	}

	def destroy = {
	}
}
