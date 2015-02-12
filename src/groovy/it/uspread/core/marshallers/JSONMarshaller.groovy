package it.uspread.core.marshallers

import grails.converters.JSON
import it.uspread.core.Message
import it.uspread.core.ReportType
import it.uspread.core.User

import java.text.SimpleDateFormat

/**
 * Configuration des formes de conversions des données vers le format JSON.<br>
 * C'est dans cette classe qu'est géré les différentes possibilités d'informations retourné aux clients.
 */
class JSONMarshaller {

    /** Configuration pour clients public : Utilisateur */
    public static final String PUBLIC_USER = "publicAPI-User"
    /** Configuration pour clients public : Utilisateur limité aux informations pour les scores */
    public static final String PUBLIC_USER_SCORE = "publicAPI-User-Score"

    /** Configuration pour clients public : Message */
    public static final String PUBLIC_MESSAGE = "publicAPI-Message"
    /** Configuration pour clients public : Message limité à son image */
    public static final String PUBLIC_MESSAGE_IMAGE = "publicAPI-Message-Image"
    /** Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages propagé ou écrits par l'utilisateur */
    public static final String PUBLIC_MESSAGE_LIST = "publicAPI-Message-List"
    /** Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages propagé ou écrits par l'utilisateur valeurs dynamique uniquement */
    public static final String PUBLIC_MESSAGE_LIST_DYNAMIC = "publicAPI-Message-List-Dynamic"
    /** Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages reçus par l'utilisateur */
    public static final String PUBLIC_MESSAGE_LIST_RECEIVED = "publicAPI-Message-List-Received"
    /** Configuration pour clients public : Message limité aux informations de retour d'une création */
    public static final String PUBLIC_MESSAGE_CREATION = "publicAPI-Message-Creation"
    /** Configuration pour clients public : Message limité aux informations de retour d'une propagation */
    public static final String PUBLIC_MESSAGE_SPREAD = "publicAPI-Message-Spread"

    /** Configuration pour clients interne */
    public static final String INTERNAL = "internalApi"

    /** Formattage utilisé pour les dates (date, heure, minute, milliseconde et timezone) */
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")

    /**
     * Enregistrement des différentes configurations publiques.
     */
    static void registerPublic() {
        // Configuration pour clients public : Utilisateur
        JSON.createNamedConfig(PUBLIC_USER) {
            it.registerObjectMarshaller(User) { User user ->
                def output = [:]
                output["id"] = user.id
                output["username"] = user.username
                output["email"] = user.email
                output["iosPushToken"] = user.iosPushToken
                return output;
            }
        }

        // Configuration pour clients public : Utilisateur limité aux informations pour les scores
        JSON.createNamedConfig(PUBLIC_USER_SCORE) {
            it.registerObjectMarshaller(User) { User user ->
                def output = [:]
                output["username"] = user.username
                return output;
            }
        }

        // Configuration pour clients public : Message
        JSON.createNamedConfig(PUBLIC_MESSAGE) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output["id"] = msg.id
                output["dateCreation"] = DATE_FORMAT.format(msg.dateCreated)

                Date date = msg.getDateReception(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output["dateReception"] = DATE_FORMAT.format(date)
                }
                date = msg.getDateSpread(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output["dateSpread"] = DATE_FORMAT.format(date)
                }

                output["nbSpread"] = msg.nbSpread
                output["text"] = msg.text
                output["textColor"] = msg.textColor
                output["backgroundColor"] = msg.backgroundColor
                output["backgroundType"] = msg.backgroundType
                // TODO
                return output;
            }
        }

        // Configuration pour clients public : Message limité à son image
        JSON.createNamedConfig(PUBLIC_MESSAGE_IMAGE) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output["id"] = msg.id
                // TODO
                return output;
            }
        }

        // Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages propagé ou écrits par l'utilisateur
        JSON.createNamedConfig(PUBLIC_MESSAGE_LIST) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output["id"] = msg.id
                output["dateCreation"] = DATE_FORMAT.format(msg.dateCreated)

                Date date = msg.getDateReception(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output["dateReception"] = DATE_FORMAT.format(date)
                }
                date = msg.getDateSpread(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output["dateSpread"] = DATE_FORMAT.format(date)
                }

                output["nbSpread"] = msg.nbSpread
                output["text"] = msg.text
                output["textColor"] = msg.textColor
                output["backgroundColor"] = msg.backgroundColor
                output["backgroundType"] = msg.backgroundType
                return output;
            }
        }

        // Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages propagé ou écrits par l'utilisateur valeurs dynamique uniquement
        JSON.createNamedConfig(PUBLIC_MESSAGE_LIST_DYNAMIC) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output["id"] = msg.id
                output["nbSpread"] = msg.nbSpread
                return output;
            }
        }

        // Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages reçus par l'utilisateur
        JSON.createNamedConfig(PUBLIC_MESSAGE_LIST_RECEIVED) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output["id"] = msg.id
                output["dateCreation"] = DATE_FORMAT.format(msg.dateCreated)

                Date date = msg.getDateReception(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output["dateReception"] = DATE_FORMAT.format(date)
                }
                date = msg.getDateSpread(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output["dateSpread"] = DATE_FORMAT.format(date)
                }

                output["text"] = msg.text
                output["textColor"] = msg.textColor
                output["backgroundColor"] = msg.backgroundColor
                output["backgroundType"] = msg.backgroundType
                return output;
            }
        }

        // Configuration pour clients public : Message limité aux informations de retour d'une création
        JSON.createNamedConfig(PUBLIC_MESSAGE_CREATION) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output["id"] = msg.id
                output["dateCreation"] = DATE_FORMAT.format(msg.dateCreated)
                return output;
            }
        }

        // Configuration pour clients public : Message limité aux informations de retour d'une propagation
        JSON.createNamedConfig(PUBLIC_MESSAGE_SPREAD) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output["id"] = msg.id
                output["dateSpread"] = DATE_FORMAT.format(msg.getDateSpread(msg.getSpringSecurityService().currentUser))
                output["nbSpread"] = msg.nbSpread
                return output;
            }
        }
    }

    /**
     * Enregistrement des différentes configurations internes.
     */
    static void registerInternal() {
        // Configuration pour une communication avec un client interne : Outillage d'usage interne (client de modération par exemple)
        JSON.createNamedConfig(INTERNAL) {
            it.registerObjectMarshaller(User) { User user ->
                def output = [:]
                output["id"] = user.id
                output["username"] = user.username
                output["email"] = user.email
                output["iosPushToken"] = user.iosPushToken
                output["role"] = user.isModerator() ? "MODERATOR" : "USER"
                if (!user.isModerator()) {
                    output["reportsSent"] = user.reportsSent
                    output["reportsReceived"] = user.reportsReceived
                    output["moderationRequired"] = user.isModerationRequired()
                }
                return output;
            }

            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output["id"] = msg.id
                output["dateCreation"] = DATE_FORMAT.format(msg.dateCreated)
                output["text"] = msg.text
                output["author"] = [id: msg.author.id, username: msg.author.username]
                def ReportType reportType = msg.getMainReportType();
                if (reportType != null) {
                    output["mainReportType"] = reportType.name()
                }

                return output;
            }
        }
    }
}
