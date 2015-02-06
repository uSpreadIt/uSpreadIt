import it.uspread.core.Role
import it.uspread.core.User
import it.uspread.core.UserRole
import it.uspread.core.marshallers.JSONMarshaller

class BootStrap {

    def init = { servletContext ->
        // Vérifie si les données de test sont déjà présente
        if (!User.count()) {
            // Création de modérateurs
            def mod = new User(username: 'mod', password: 'mod', email:"mod@free.fr", specialUser: true)

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

        // Configuration de la conversion Domaine->JSON
        JSONMarshaller.register()
    }

    def destroy = {
    }
}
