import grails.util.Environment
import it.uspread.core.domain.Role
import it.uspread.core.domain.User
import it.uspread.core.domain.UserRole
import it.uspread.core.json.JSONMarshaller

class BootStrap {

    def init = { servletContext ->

        if (Environment.current == Environment.TEST || Environment.current == Environment.DEVELOPMENT) {
            // NE PAS CHANGER L'ORDRE CAR LES test fonctionels se basent la dessus

            // création de 7 users (Les 2 derniers sont pour des tests de suppression)
            for (int i = 1; i <= 7; i++){
                new User(username: 'user'+i, password: 'user'+i, email:"user"+i+"@42.fr").save()
            }

            // Création de 2 modérateur (Le dernier est pour un test de suppression)
            def roleMod = new Role(authority: Role.ROLE_MODERATOR)
            roleMod.save()

            def mod = new User(username: 'mod', password: 'mod', email:"mod@42.fr", specialUser: true)
            mod.save()
            UserRole droitsMod = new UserRole(user: mod, role: roleMod)
            droitsMod.save()

            mod = new User(username: 'old_mod', password: 'old_mod', email:"old_mod@42.fr", specialUser: true)
            mod.save()
            droitsMod = new UserRole(user: mod, role: roleMod)
            droitsMod.save()
        } else if (Environment.current == Environment.PRODUCTION) {
            // Si BD vierge.
            if (!User.count()) {
                // Création du modérateur originel  (FIXME à remplacer plus tard plutôt par un administrateur)
                def mod = new User(username: 'mod', password: 'mod', email:"mod@free.fr", specialUser: true)
                mod.save([failOnError: true])

                def roleModerator = new Role(authority: Role.ROLE_MODERATOR)
                roleModerator.save([failOnError: true])
                def roleAdministrator = new Role(authority: Role.ROLE_ADMINISTRATOR)
                roleAdministrator.save([failOnError: true])

                UserRole droitsMod = new UserRole(user: mod, role: roleModerator)
                droitsMod.save([failOnError: true])

                // création de plein d'users de test (FIXME temporaire)
                for (int i = 1; i <= 5; i++){
                    new User(username: 'user'+i, password: 'user'+i, email:"user"+i+"@free.fr").save([failOnError: true])
                }
            }
        }



        // Configuration de la conversion Domaine->JSON
        JSONMarshaller.registerPublic()
        JSONMarshaller.registerInternal()
    }

    def destroy = {
    }
}
