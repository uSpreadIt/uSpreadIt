package it.uspread.core.marshallers

import grails.converters.JSON
import it.uspread.core.Message
import it.uspread.core.ReportType
import it.uspread.core.User

import java.text.SimpleDateFormat

/**
 * Configuration des conversions des objets du domaine vers le format JSON.<br>
 * Les configurations diffèrent suivant le type d'utilisateur utilisant ce service
 */
class JSONMarshaller {

    /** Configuration de conversion vers clients public */
    public static final String PUBLIC_MARSHALLER = "publicApi"
    /** Configuration de conversion vers clients interne */
    public static final String INTERNAL_MARSHALLER = "internalApi"

    /** Formattage utilisé pour les dates (date, heure, minute, milliseconde et timezone) */
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")

    static void register() {
        // Configuration pour une communication avec un client simple utilisateur du service (Clients public : mobiles)
        JSON.createNamedConfig(PUBLIC_MARSHALLER) {
            it.registerObjectMarshaller(User) { User user ->
                def output = [:]
                output["id"] = user.id
                output["username"] = user.username
                output["email"] = user.email
                output["iosPushToken"] = user.iosPushToken
                return output;
            }

            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output["id"] = msg.id
                output["creationDate"] = DATE_FORMAT.format(msg.dateCreated)
                //output["receptionDate"] = DATE_FORMAT.format(msg.receivedBy.find({it.user == (User) springSecurityService.currentUser}).dateReception) TODO
                //output["spreadDate"] = DATE_FORMAT.format(msg.spreadBy.find({it.user == (User) springSecurityService.currentUser}).dateSpread) TODO
                output["nbSpread"] = msg.nbSpread
                output["text"] = msg.text
                output["textColor"] = msg.textColor
                output["backgroundColor"] = msg.backgroundColor
                output["backgroundType"] = msg.backgroundType
                return output;
            }
        }

        // Configuration pour une communication avec un client modérateur ou administrateur du service
        // (Clients interne : Outils de modération ou d'administration)
        JSON.createNamedConfig(INTERNAL_MARSHALLER) {
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
                output["creationDate"] = DATE_FORMAT.format(msg.dateCreated)
                output["text"] = msg.text
                output["author"] = [id: msg.author.id, username: msg.author.username]
                def ReportType reportType = msg.getMainReportType();
                if (reportType != null) {
                    output["reportType"] = reportType.name()
                }

                return output;
            }
        }
    }
}
