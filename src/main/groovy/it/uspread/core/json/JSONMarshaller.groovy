package it.uspread.core.json

import grails.converters.JSON
import it.uspread.core.data.Status
import it.uspread.core.domain.Message
import it.uspread.core.domain.User
import it.uspread.core.type.BackgroundType
import it.uspread.core.type.ReportType

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
    /** Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages écrits par l'utilisateur */
    public static final String PUBLIC_MESSAGE_LIST_WRITED = "publicAPI-Message-List-Writed"
    /** Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages propagé par l'utilisateur */
    public static final String PUBLIC_MESSAGE_LIST_SPREAD = "publicAPI-Message-List-Spread"
    /** Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages reçus par l'utilisateur */
    public static final String PUBLIC_MESSAGE_LIST_RECEIVED = "publicAPI-Message-List-Received"
    /** Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages propagé ou écrits par l'utilisateur valeurs dynamique uniquement */
    public static final String PUBLIC_MESSAGE_LIST_DYNAMIC = "publicAPI-Message-List-Dynamic"
    /** Configuration pour clients public : Message limité aux informations de retour d'une création */
    public static final String PUBLIC_MESSAGE_CREATION = "publicAPI-Message-Creation"
    /** Configuration pour clients public : Message limité aux informations de retour d'une propagation */
    public static final String PUBLIC_MESSAGE_SPREAD = "publicAPI-Message-Spread"

    /** Configuration pour clients public : Statut de l'utilisateur */
    public static final String PUBLIC_STATUS = "publicAPI-Status"
    /** Configuration pour clients public : Statut de l'utilisateur limité aux informations de quota */
    public static final String PUBLIC_STATUS_QUOTA = "publicAPI-Status-Quota"

    /** Configuration pour clients interne */
    public static final String INTERNAL = "internalApi"

    /** Formattage utilisé pour les dates (date, heure, minute, milliseconde et timezone) */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")

    /**
     * Enregistrement des différentes configurations publiques.
     */
    static void registerPublic() {
        // Configuration pour clients public : Utilisateur
        JSON.createNamedConfig(PUBLIC_USER) {
            it.registerObjectMarshaller(User) { User user ->
                def output = [:]
                output[JSONAttribute.USER_ID] = user.id
                output[JSONAttribute.USER_USERNAME] = user.username
                output[JSONAttribute.USER_EMAIL] = user.email
                output[JSONAttribute.USER_DATECREATED] = user.dateCreated
                output[JSONAttribute.USER_ACCOUNTVERIFIED] = user.accountVerified
                output[JSONAttribute.USER_PREFLANGUAGE] = user.preferredLanguage.name()
                if (user.location != null) {
                    output[JSONAttribute.USER_LOCATION] = user.location
                }
                output[JSONAttribute.USER_MESSAGELOCATED] = user.messageLocated
                return output
            }
        }

        // Configuration pour clients public : Utilisateur limité aux informations pour les scores
        JSON.createNamedConfig(PUBLIC_USER_SCORE) {
            it.registerObjectMarshaller(User) { User user ->
                def output = [:]
                output[JSONAttribute.USER_USERNAME] = user.username
                return output
            }
        }

        // Configuration pour clients public : Message
        JSON.createNamedConfig(PUBLIC_MESSAGE) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output[JSONAttribute.MESSAGE_ID] = msg.id
                output[JSONAttribute.MESSAGE_TYPE] = msg.type.name()
                output[JSONAttribute.MESSAGE_DATECREATED] = DATE_FORMAT.format(msg.dateCreated)

                Date date = msg.getDateReceived(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output[JSONAttribute.MESSAGE_DATERECEIVED] = DATE_FORMAT.format(date)
                }
                date = msg.getDateSpread(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output[JSONAttribute.MESSAGE_DATESPREAD] = DATE_FORMAT.format(date)
                }

                output[JSONAttribute.MESSAGE_NBSPREAD] = msg.nbSpread
                if (msg.text != null) {
                    output[JSONAttribute.MESSAGE_TEXT] = msg.text
                }
                output[JSONAttribute.MESSAGE_TEXTCOLOR] = msg.textColor
                output[JSONAttribute.MESSAGE_BACKGROUNDTYPE] = msg.backgroundType.name()
                if (msg.backgroundType == BackgroundType.PLAIN) {
                    output[JSONAttribute.MESSAGE_BACKGROUNDCOLOR] = msg.backgroundColor
                }
                if (msg.link != null) {
                    output[JSONAttribute.MESSAGE_LINK] = msg.link
                }
                if (msg.backgroundType == BackgroundType.IMAGE) {
                    output[JSONAttribute.MESSAGE_BACKGROUNDIMAGE] = msg.backgroundImage.image
                }

                return output
            }
        }

        // Configuration pour clients public : Message limité à son image
        JSON.createNamedConfig(PUBLIC_MESSAGE_IMAGE) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output[JSONAttribute.MESSAGE_ID] = msg.id
                output[JSONAttribute.MESSAGE_BACKGROUNDIMAGE] = msg.backgroundImage.image
                return output
            }
        }

        // Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages écrits par l'utilisateur
        JSON.createNamedConfig(PUBLIC_MESSAGE_LIST_WRITED) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output[JSONAttribute.MESSAGE_ID] = msg.id
                output[JSONAttribute.MESSAGE_TYPE] = msg.type.name()
                output[JSONAttribute.MESSAGE_DATECREATED] = DATE_FORMAT.format(msg.dateCreated)
                output[JSONAttribute.MESSAGE_NBSPREAD] = msg.nbSpread
                if (msg.text != null) {
                    output[JSONAttribute.MESSAGE_TEXT] = msg.text
                }
                output[JSONAttribute.MESSAGE_TEXTCOLOR] = msg.textColor
                output[JSONAttribute.MESSAGE_BACKGROUNDTYPE] = msg.backgroundType.name()
                if (msg.backgroundType == BackgroundType.PLAIN) {
                    output[JSONAttribute.MESSAGE_BACKGROUNDCOLOR] = msg.backgroundColor
                }
                if (msg.link != null) {
                    output[JSONAttribute.MESSAGE_LINK] = msg.link
                }
                if (msg.backgroundType == BackgroundType.IMAGE) {
                    output[JSONAttribute.MESSAGE_BACKGROUNDIMAGE] = msg.backgroundImage.image
                }
                return output
            }
        }

        // Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages propagé par l'utilisateur
        JSON.createNamedConfig(PUBLIC_MESSAGE_LIST_SPREAD) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output[JSONAttribute.MESSAGE_ID] = msg.id
                output[JSONAttribute.MESSAGE_TYPE] = msg.type.name()

                Date date = msg.getDateSpread(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output[JSONAttribute.MESSAGE_DATESPREAD] = DATE_FORMAT.format(date)
                }

                output[JSONAttribute.MESSAGE_NBSPREAD] = msg.nbSpread
                if (msg.text != null) {
                    output[JSONAttribute.MESSAGE_TEXT] = msg.text
                }
                output[JSONAttribute.MESSAGE_TEXTCOLOR] = msg.textColor
                output[JSONAttribute.MESSAGE_BACKGROUNDTYPE] = msg.backgroundType.name()
                if (msg.backgroundType == BackgroundType.PLAIN) {
                    output[JSONAttribute.MESSAGE_BACKGROUNDCOLOR] = msg.backgroundColor
                }
                if (msg.link != null) {
                    output[JSONAttribute.MESSAGE_LINK] = msg.link
                }
                if (msg.backgroundType == BackgroundType.IMAGE) {
                    output[JSONAttribute.MESSAGE_BACKGROUNDIMAGE] = msg.backgroundImage.image
                }
                return output
            }
        }

        // Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages reçus par l'utilisateur
        JSON.createNamedConfig(PUBLIC_MESSAGE_LIST_RECEIVED) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output[JSONAttribute.MESSAGE_ID] = msg.id
                output[JSONAttribute.MESSAGE_TYPE] = msg.type.name()

                Date date = msg.getDateReceived(msg.getSpringSecurityService().currentUser)
                if (date != null) {
                    output[JSONAttribute.MESSAGE_DATERECEIVED] = DATE_FORMAT.format(date)
                }

                if (msg.text != null) {
                    output[JSONAttribute.MESSAGE_TEXT] = msg.text
                }
                output[JSONAttribute.MESSAGE_TEXTCOLOR] = msg.textColor
                output[JSONAttribute.MESSAGE_BACKGROUNDTYPE] = msg.backgroundType.name()
                if (msg.backgroundType == BackgroundType.PLAIN) {
                    output[JSONAttribute.MESSAGE_BACKGROUNDCOLOR] = msg.backgroundColor
                }
                if (msg.link != null) {
                    output[JSONAttribute.MESSAGE_LINK] = msg.link
                }
                if (msg.backgroundType == BackgroundType.IMAGE) {
                    output[JSONAttribute.MESSAGE_BACKGROUNDIMAGE] = msg.backgroundImage.image
                }
                return output
            }
        }

        // Configuration pour clients public : Liste de Message limité aux informations nécessaire aux messages propagé ou écrits par l'utilisateur valeurs dynamique uniquement
        JSON.createNamedConfig(PUBLIC_MESSAGE_LIST_DYNAMIC) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output[JSONAttribute.MESSAGE_ID] = msg.id
                output[JSONAttribute.MESSAGE_NBSPREAD] = msg.nbSpread
                return output
            }
        }

        // Configuration pour clients public : Message limité aux informations de retour d'une création
        JSON.createNamedConfig(PUBLIC_MESSAGE_CREATION) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output[JSONAttribute.MESSAGE_ID] = msg.id
                output[JSONAttribute.MESSAGE_DATECREATED] = DATE_FORMAT.format(msg.dateCreated)
                return output
            }
        }

        // Configuration pour clients public : Message limité aux informations de retour d'une propagation
        JSON.createNamedConfig(PUBLIC_MESSAGE_SPREAD) {
            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output[JSONAttribute.MESSAGE_ID] = msg.id
                output[JSONAttribute.MESSAGE_DATESPREAD] = DATE_FORMAT.format(msg.getDateSpread(msg.getSpringSecurityService().currentUser))
                output[JSONAttribute.MESSAGE_NBSPREAD] = msg.nbSpread
                return output
            }
        }

        // Configuration pour clients public : Statut de l'utilisateur
        JSON.createNamedConfig(PUBLIC_STATUS) {
            it.registerObjectMarshaller(Status) { Status status ->
                def output = [:]
                output[JSONAttribute.STATUS_WORLDQUOTAREACHED] = status.worldQuotaReached
                output[JSONAttribute.STATUS_LOCALQUOTAREACHED] = status.localQuotaReached
                output[JSONAttribute.STATUS_NBMESSAGEWRITED] = status.nbMessageWrited
                output[JSONAttribute.STATUS_NBMESSAGESPREAD] = status.nbMessageSpread
                output[JSONAttribute.STATUS_NBMESSAGEIGNORED] = status.nbMessageIgnored
                output[JSONAttribute.STATUS_NBMESSAGEREPORTED] = status.nbMessageReported
                return output
            }
        }

        // Configuration pour clients public : Statut de l'utilisateur limité à l'information de quota
        JSON.createNamedConfig(PUBLIC_STATUS_QUOTA) {
            it.registerObjectMarshaller(Status) { Status status ->
                def output = [:]
                output[JSONAttribute.STATUS_WORLDQUOTAREACHED] = status.worldQuotaReached
                output[JSONAttribute.STATUS_LOCALQUOTAREACHED] = status.localQuotaReached
                return output
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
                output[JSONAttribute.USER_ID] = user.id
                output[JSONAttribute.USER_USERNAME] = user.username
                output[JSONAttribute.USER_EMAIL] = user.email
                output[JSONAttribute.USER_ROLE] = user.isModerator() ? 'MODERATOR' : (user.isAdministrator() ? 'ADMINISTRATOR' : 'PUBLIC')
                if (!user.isModerator()) {
                    output[JSONAttribute.USER_REPORTSSENT] = user.reportsSent
                    output[JSONAttribute.USER_REPORTSRECEIVED] = user.reportsReceived
                    output[JSONAttribute.USER_MODERATIONREQUIRED] = user.isModerationRequired()
                }
                return output
            }

            it.registerObjectMarshaller(Message) { Message msg ->
                def output = [:]
                output[JSONAttribute.MESSAGE_ID] = msg.id
                output[JSONAttribute.MESSAGE_DATECREATED] = DATE_FORMAT.format(msg.dateCreated)
                if (msg.text != null) {
                    output[JSONAttribute.MESSAGE_TEXT] = msg.text
                }
                output[JSONAttribute.MESSAGE_AUTHOR] = ["${JSONAttribute.USER_ID}": msg.author.id, "${JSONAttribute.USER_USERNAME}": msg.author.username]
                if (msg.isReported()) {
                    def ReportType reportType = msg.getMainReportType()
                    output[JSONAttribute.MESSAGE_MAINREPORTTYPE] = reportType.name()
                    if ( msg.backgroundType == BackgroundType.IMAGE) {
                        output[JSONAttribute.MESSAGE_BACKGROUNDIMAGE] = msg.backgroundImage.image
                    }
                    if (msg.link != null) {
                        output[JSONAttribute.MESSAGE_LINK] = msg.link
                    }
                }

                return output
            }
        }
    }
}
