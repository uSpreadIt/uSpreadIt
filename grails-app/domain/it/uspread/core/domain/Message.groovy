package it.uspread.core.domain

import it.uspread.core.type.BackgroundType
import it.uspread.core.type.ReportType

/**
 * Modèle des messages.
 */
class Message {

    /** Nombre de caractère maximal d'un message */
    public static final int TEXT_MAX_LENGTH = 240

    transient springSecurityService

    /** Auteur */
    User author
    /** Nombre de propagation */
    long nbSpread
    /** Date de création du message */
    Date dateCreated // Ne pas renommer car ce nom est un nom spécial détécté par Grails cf autoTimestamp
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
        author(index: 'author_idx')
        text(length: TEXT_MAX_LENGTH)
        textColor(length: 6)
        backgroundType(enumType: 'string')
        //backgroundImage(cascade: 'all-delete-orphan')
        receivedBy(cascade: 'all-delete-orphan')
        spreadBy(cascade: 'all-delete-orphan')
        ignoredBy(joinTable: [name: 'message_ignored', key: 'message_id', column: 'ignored_by_user_id'])
        reports(cascade: 'all-delete-orphan')
    }

    static constraints = {
        text(maxsize: TEXT_MAX_LENGTH)
        textColor(size: 6..6)
        backgroundColor(nullable: true)
        backgroundImage(nullable: true)
    }

    /**
     * Indique le type de signalement principalement effectué
     * @return type de signalement
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
     * Si auteur ou modérateur ou message reçus/propagé
     * @param user l'utilisateur en question
     * @return Vrai si autorisé
     */
    boolean isUserAllowedToRead(User user){
        return author.id == user.id || user?.isModerator() || receivedBy?.any({it.user.id == user.id}) || spreadBy?.any({it.user.id == user.id})
    }

    /**
     * Indique si l'utilisateur est autorisé à supprimer ce message
     * @param user l'utilisateur en question
     * @return Vrai si autorisé
     */
    boolean isUserAllowedToDelete(User user){
        return author.id == user.id || user?.isModerator()
    }

    /**
     * Recherche de la date de réception du message pour l'utilisateur donné
     * @param user Un utilisateur
     * @return Date de réception ou null si non reçus par l'utilisateur
     */
    Date getDateReception(User user) {
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
