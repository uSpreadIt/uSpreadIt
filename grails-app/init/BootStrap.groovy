import grails.util.Environment
import it.uspread.core.domain.Role
import it.uspread.core.domain.User
import it.uspread.core.domain.UserRole
import it.uspread.core.json.JSONMarshaller

class BootStrap {

    def init = { servletContext ->

        if (Environment.current == Environment.TEST || Environment.current == Environment.DEVELOPMENT) {
            // NE PAS CHANGER L'ORDRE CAR LES test fonctionels se basent la dessus

            // Création des roles disponibles
            def roleUser = new Role(authority: Role.ROLE_USER)
            roleUser.save()
            def roleModerator = new Role(authority: Role.ROLE_MODERATOR)
            roleModerator.save()
            def roleAdministrator = new Role(authority: Role.ROLE_ADMINISTRATOR)
            roleAdministrator.save()

            // création de 7 users (Les 2 derniers sont pour des tests de suppression)
            for (int i = 1; i <= 7; i++){
                def user = new User(username: 'user'+i, password: 'user'+i, email:"user"+i+"@42.fr").save()
                UserRole.create(user, roleUser)
            }

            // Création de 2 modérateur (Le dernier est pour un test de suppression)
            def mod = new User(username: 'mod', password: 'mod', email:"mod@42.fr", specialUser: true)
            mod.save()
            UserRole.create(mod, roleModerator)

            mod = new User(username: 'old_mod', password: 'old_mod', email:"old_mod@42.fr", specialUser: true)
            mod.save()
            UserRole.create(mod, roleModerator)
        } else if (Environment.current == Environment.PRODUCTION) {
            // Si BD vierge alors on initialise quelques données.
            if (!Role.count()) {

                // Création des roles disponibles
                def roleUser = new Role(authority: Role.ROLE_USER)
                roleUser.save([failOnError: true])
                def roleModerator = new Role(authority: Role.ROLE_MODERATOR)
                roleModerator.save([failOnError: true])
                def roleAdministrator = new Role(authority: Role.ROLE_ADMINISTRATOR)
                roleAdministrator.save([failOnError: true])

                // Création du modérateur originel  (FIXME à remplacer plus tard plutôt par un administrateur)
                def mod = new User(username: 'mod', password: 'mod', email:"mod@free.fr", specialUser: true)
                mod.save([failOnError: true])
                UserRole.create(mod, roleModerator)

                // création de plein d'users de test (FIXME temporaire)
                for (int i = 1; i <= 5; i++){
                    def user = new User(username: 'user'+i, password: 'user'+i, email:"user"+i+"@free.fr").save([failOnError: true])
                    UserRole.create(user, roleUser)
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
