import grails.util.Environment
import it.uspread.core.domain.Role
import it.uspread.core.domain.Setting
import it.uspread.core.domain.User
import it.uspread.core.domain.UserRole
import it.uspread.core.json.JSONMarshaller

class BootStrap {

    def init = { servletContext ->

        // #############################################################################################
        if (Environment.current == Environment.PRODUCTION) {
            // Si BD vierge alors on initialise quelques données.
            if (!Role.count()) {
                def setting = new Setting() // FIXME a changer par les valeurs cibles quand on pourra les modifier via l'outillage interne
                setting.maxUser = 1000
                setting.maxReceivedMessageByUser = 50
                setting.maxCreateWorldMessageByDayForUser = 10
                setting.maxCreateWorldMessageByDayForPremiumUser = 20
                setting.maxCreateLocalMessageByDayForUser = 10
                setting.maxCreateLocalMessageByDayForPremiumUser = 20
                setting.nbUserForInitialWorldSpread = 20
                setting.nbUserForWorldSpread = 10
                setting.nbUserForInitialLocalSpread = 20
                setting.nbUserForLocalSpread = 10
                setting.save()

                // Création des roles disponibles
                def rolePublic = new Role(authority: Role.ROLE_PUBLIC)
                rolePublic.save([failOnError: true])
                def roleModerator = new Role(authority: Role.ROLE_MODERATOR)
                roleModerator.save([failOnError: true])
                def roleAdministrator = new Role(authority: Role.ROLE_ADMINISTRATOR)
                roleAdministrator.save([failOnError: true])

                // Création de l'administrateur originel // FIXME par principe changer le mot de passe et l'user
                def admin = new User(username: 'admin', password: 'admin', email:"admin@uspread.it", publicUser: false)
                admin.save([failOnError: true])
                UserRole.create(admin, roleAdministrator)

                // Création d'un modérateur (FIXME Retirer cette création quand l'outillage interne permettra à l'administrateur d'ajouter des modérateur)
                def mod = new User(username: 'mod', password: 'mod', email:"mod@uspread.it", publicUser: false)
                mod.save([failOnError: true])
                UserRole.create(mod, roleModerator)

                // création de plein d'users de test (FIXME temporaire : à supprimer)
                for (int i = 1; i <= 5; i++){
                    def user = new User(username: 'user'+i, password: 'user'+i, email:"user"+i+"@42.fr", publicUser: true)
                    user.save([failOnError: true])
                    UserRole.create(user, rolePublic)
                }
            }
        }
        // #############################################################################################
        else if (Environment.current == Environment.TEST || Environment.current == Environment.DEVELOPMENT) {
            // NE PAS CHANGER L'ORDRE CAR LES test fonctionels se basent sur les id des éléments créé ici

            def setting = new Setting()
            setting.maxUser = 20
            setting.maxReceivedMessageByUser = 10
            setting.maxCreateWorldMessageByDayForUser = 5
            setting.maxCreateWorldMessageByDayForPremiumUser = 6
            setting.maxCreateLocalMessageByDayForUser = 5
            setting.maxCreateLocalMessageByDayForPremiumUser = 6
            setting.nbUserForInitialWorldSpread = 15
            setting.nbUserForWorldSpread = 10
            setting.nbUserForInitialLocalSpread = 15
            setting.nbUserForLocalSpread = 10
            setting.save()

            // Création des roles disponibles
            def rolePublic = new Role(authority: Role.ROLE_PUBLIC)
            rolePublic.save()
            def roleModerator = new Role(authority: Role.ROLE_MODERATOR)
            roleModerator.save()
            def roleAdministrator = new Role(authority: Role.ROLE_ADMINISTRATOR)
            roleAdministrator.save()

            // création de 7 users (Les 2 derniers sont pour des tests de suppression)
            for (int i = 1; i <= 7; i++){
                def user = new User(username: 'user'+i, password: 'user'+i, email:"user"+i+"@42.fr", publicUser: true)
                user.save()
                UserRole.create(user, rolePublic)
            }

            // Création de 2 modérateur (Le dernier est pour un test de suppression)
            def mod = new User(username: 'mod', password: 'mod', email:"mod@uspread.it", publicUser: false)
            mod.save()
            UserRole.create(mod, roleModerator)

            mod = new User(username: 'old_mod', password: 'old_mod', email:"old_mod@uspread.it", publicUser: false)
            mod.save()
            UserRole.create(mod, roleModerator)

            // Création de l'administrateur originel
            def admin = new User(username: 'admin', password: 'admin', email:"admin@uspread.it", publicUser: false)
            admin.save()
            UserRole.create(admin, roleAdministrator)
        }

        // Configuration de la conversion Domaine->JSON
        JSONMarshaller.registerPublic()
        JSONMarshaller.registerInternal()
    }

    def destroy = {
    }
}
