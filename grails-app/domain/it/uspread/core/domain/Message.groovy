package it.uspread.core.domain

import it.uspread.core.type.BackgroundType
import it.uspread.core.type.Language
import it.uspread.core.type.MessageType
import it.uspread.core.type.ReportType

/**
 * Modèle des messages.
 */
class Message {

    /** Nombre de caractère maximal d'un message */
    public static final int TEXT_MAX_LENGTH = 240

    /** Nombre de caractère maximal d'un lien web */
    public static final int LINK_MAX_LENGTH = 255

    /** Nombre de caractère maximal d'une geolocalisation */
    public static final int LOCATION_MAX_LENGTH = 25

    transient springSecurityService

    /** Auteur */
    User author
    /** Type du message */
    MessageType type
    /** Date de création du message */
    Date dateCreated // Ne pas renommer car ce nom est un nom spécial détécté par Grails cf autoTimestamp
    /** Nombre de propagation */
    long nbSpread
    /** Texte du message */
    String text
    /** Couleur du texte (code couleur HTML sans le '#') */
    String textColor
    /** Type de remplissement du cadre {@link BackgroundType} */
    BackgroundType backgroundType
    /** Couleur de fond du cadre (HTML code) */
    String backgroundColor
    /** Image de fond */
    Image backgroundImage
    /** Language probable indentifié */
    Language language
    /** Lien du message */
    String link
    /** Géolocalisation du lieu de création du message (latitude,longitude) */
    String location

    /** La liste utilisateurs ayant reçus ce message */
    Set<Spread> receivedBy
    /** La liste utilisateurs ayant propagé ce message */
    Set<Spread> spreadBy
    /** La liste des utilisateurs ayant ignoré ce message */
    Set<User> ignoredBy
    /** La liste des signalements effectué sur ce message */
    Set<Report> reports

    static belongsTo = User

    static hasMany = [receivedBy: Spread, spreadBy: Spread, ignoredBy: User, reports: Report]

    static mappedBy = [receivedBy: 'messageReceived', spreadBy: 'messageSpread', reports: 'message']

    static mapping = {
        version(true)
        table('message')
        id([generator:'sequence', params:[sequence:'message_sequence']])
        author(index: 'idx_message_author')
        type(enumType: 'string', length: 5, index: 'idx_message_type')
        text(length: TEXT_MAX_LENGTH)
        textColor(length: 6)
        backgroundType(enumType: 'string', length: 10)
        backgroundImage(cascade: 'all-delete-orphan')
        language(enumType: 'string', length: 2)
        link(length: LINK_MAX_LENGTH)
        location(length: LOCATION_MAX_LENGTH)
        receivedBy(cascade: 'all-delete-orphan')
        spreadBy(cascade: 'all-delete-orphan')
        ignoredBy(joinTable: [name: 'message_ignored', key: 'message_id', column: 'user_id'])
        reports(cascade: 'all-delete-orphan')
    }

    static constraints = {
        text(nullable: true, maxsize: TEXT_MAX_LENGTH, validator: { val, obj ->
            // Autorisé un texte null que si une image de fond est associé
            return obj.backgroundImage == null && val != null ||  obj.backgroundImage != null
        })
        textColor(size: 6..6)
        backgroundColor(nullable: true)
        backgroundImage(nullable: true)
        language(nullable: true)
        link(nullable: true, maxsize: LINK_MAX_LENGTH)
        location(nullable: true, maxsize: LOCATION_MAX_LENGTH)
    }

    /**
     * Indique le type de signalement principalement effectué
     * @return type de signalement ou null si non signalé
     */
    ReportType getMainReportType() {
        int reportedAsSpam = 0
        int reportedAsThreat = 0
        int reportedAsInappropriate = 0
        reports?.each({ Report r ->
            switch (r.type) {
                case ReportType.SPAM :
                    reportedAsSpam++
                    break
                case ReportType.THREAT :
                    reportedAsThreat++
                    break
                case ReportType.INAPPROPRIATE :
                    reportedAsInappropriate++
                    break
            }
        })

        if (reportedAsSpam + reportedAsThreat + reportedAsInappropriate == 0) {
            return null
        }
        else if (reportedAsSpam >= reportedAsThreat && reportedAsSpam >= reportedAsInappropriate) {
            return ReportType.SPAM
        }
        else if (reportedAsThreat >= reportedAsSpam && reportedAsThreat >= reportedAsInappropriate) {
            return ReportType.THREAT
        }
        else if (reportedAsInappropriate >= reportedAsSpam && reportedAsInappropriate >= reportedAsThreat) {
            return ReportType.INAPPROPRIATE
        }
        return null
    }

    /**
     * Indique si le message a été signalé
     * @return Vrai si signalé
     */
    boolean isReported() {
        return !reports?.isEmpty()
    }

    /**
     * Indique si l'utilisateur est autorisé à lire ce message<br>
     * Si auteur ou modérateur/admin ou message reçus/propagé
     * @param user l'utilisateur en question
     * @return Vrai si autorisé
     */
    boolean isUserAllowedToRead(User user){
        return author.id == user.id || !(user.publicUser) || receivedBy?.any({it.user.id == user.id}) || spreadBy?.any({it.user.id == user.id})
    }

    /**
     * Indique si l'utilisateur est autorisé à supprimer ce message
     * @param user l'utilisateur en question
     * @return Vrai si autorisé
     */
    boolean isUserAllowedToDelete(User user){
        return author.id == user.id || !(user.publicUser)
    }

    /**
     * Recherche de la date de réception du message pour l'utilisateur donné
     * @param user Un utilisateur
     * @return Date de réception ou null si non reçus par l'utilisateur
     */
    Date getDateReceived(User user) {
        Spread spread = receivedBy?.find({ it.user.id == user.id })
        return spread?.date
    }

    /**
     * Recherche de la date de propagation du message pour l'utilisateur donné
     * @param user Un utilisateur
     * @return Date de propagation ou null si non reçus par l'utilisateur
     */
    Date getDateSpread(User user) {
        Spread spread = spreadBy?.find({ it.user.id == user.id })
        return spread?.date
    }

    /**
     * Recherche l'information de réception de ce message pour l'user donné
     * @param user Un utilisateur
     * @return
     */
    Spread getReceivedFor(User user) {
        return receivedBy?.find({ it.user.id == user.id })
    }

    /**
     * Recherche l'information de propagation de ce message pour l'user donné
     * @param user Un utilisateur
     * @return
     */
    Spread getSpreadFor(User user) {
        return spreadBy?.find({ it.user.id == user.id })
    }

    /**
     * Recherche l'information de signalement de ce message par l'user donné
     * @param user Un utilisateur
     * @return
     */
    Report getReportBy(User user) {
        return reports?.find({ it.reporter.id == user.id })
    }

    String toString(){
        if (text){
            if (30 >= text.size()){
                return text
            } else {
                return text.substring(0,30) + "..."
            }
        } else {
            return "<EMPTY>"
        }
    }
}
