package it.uspread.core.marshallers

import grails.converters.JSON
import it.uspread.core.Message
import it.uspread.core.User

import java.text.SimpleDateFormat

/**
 * Configuration des conversions des objets du domaine vers le format JSON.<br>
 * Les configurations diffèrent suivant le type d'utilisateur utilisant ce service (ce qui détermine l'application cliente)
 */
class JSONMarshaller {

	/** Configuration de conversion vers clients public */
	public static final String PUBLIC_MARSHALLER = "publicApi"
	/** Configuration de conversion vers clients interne */
	public static final String INTERNAL_MARSHALLER = "internalApi"

	/** Formattage utilisé pour les dates (date, heure, minute et timezone) */
	public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

	static void register() {
		// Configuration pour une communication avec un client simple utilisateur du service (Clients public : mobiles)
		JSON.createNamedConfig(PUBLIC_MARSHALLER) {
			it.registerObjectMarshaller(User) {
				def output = [:]
				output["id"] = it.id
				output["username"] = it.username
				output["email"] = it.email
				return output;
			}

			it.registerObjectMarshaller(Message) {
				def output = [:]
				output["id"] = it.id
				output["creationDate"] = DATE_FORMAT.format(it.dateCreated)
				output["nbSpread"] = it.nbSpread
				output["text"] = it.text
				output["textColor"] = it.textColor
				output["backgroundColor"] = it.backgroundColor
				output["backgroundType"] = it.backgroundType
				return output;
			}
		}

		// Configuration pour une communication avec un client modérateur ou administrateur du service
		// (Clients interne : Outils de modération ou d'administration)
		JSON.createNamedConfig(INTERNAL_MARSHALLER) {
			it.registerObjectMarshaller(User) {
				def output = [:]
				output["id"] = it.id
				output["username"] = it.username
				output["email"] = it.email
				output["role"] = it.isModerator() ? "MODERATOR" : "USER"
				return output;
			}

			it.registerObjectMarshaller(Message) {
				def output = [:]
				output["id"] = it.id
				output["creationDate"] = DATE_FORMAT.format(it.dateCreated)
				output["text"] = it.text
				output["author"] = [id: it.author.id, username: it.author.username]

				return output;
			}
		}
	}
}
