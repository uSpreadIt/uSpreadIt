import it.uspread.core.Message
import it.uspread.core.Role
import it.uspread.core.User
import it.uspread.core.UserRole
import it.uspread.core.marshallers.JSONMarshaller

class BootStrap {

	def init = { servletContext ->
		// Vérifie si les données de test sont déjà présente
		if (!Message.count()) {
//			def chuck = new User(username: 'chuck', password: 'chuck', email:"ChuckNoris@42.fr")
//            def etienne = new User(username: 'etienne', password: 'etienne', email:"Etienne@free.fr")
            def mod = new User(username: 'mod', password: 'mod', email:"mod@free.fr", specialUser: true)

//            chuck.addToMessages(new Message(text:"Un message de chuck transmis à etienne", sentTo:[etienne]))
//            chuck.addToMessages(new Message(text:"Un autre de chuck propagé par etienne", spreadBy: [etienne]))
//            chuck.addToMessages(new Message(text:"Un autre de chuck signalé par etienne", reportedBy: [etienne]))
//            chuck.addToMessages(new Message(text:"Un autre de chuck (impossible)"))
//			chuck.save(failOnError: true)
//
//            etienne.addToMessages(new Message(text:"Un message de Etienne transmis à chuck", sentTo:[chuck]))
//            etienne.addToMessages(new Message(text:"Un autre de Etienne propagé par chuck", spreadBy: [chuck]))
//            etienne.addToMessages(new Message(text:"Un autre de Etienne signalé par chuck", reportedBy: [chuck]))
//			etienne.save(failOnError: true)


            def roleMod = new Role(authority: Role.ROLE_MODERATOR)
            roleMod.save(failOnError: true)
            mod.save(failOnError: true)
            UserRole droitsMod = new UserRole(user: mod, role: roleMod)
            droitsMod.save(failOnError: true)

            // création de plein d'users
            for (int i = 1; i<=5; i++){
                new User(username: 'user'+i, password: 'user'+i, email:"user"+i+"@free.fr").save(failOnError: true)
            }
		}

		JSONMarshaller.register()
	}

	def destroy = {
	}
}
