package it.uspread.core

import it.uspread.core.type.ReportType

/**
 * Modèle du message
 */
class Message {

    transient springSecurityService

    /** Auteur */
    User author
    /** Nombre de propagation */
    long nbSpread
    /** Date de création */
    Date dateCreated
    /** Texte du message */
    String text
    /** Couleur du texte (HTML code) */
    String textColor = "#000000"
    /** Couleur de fond du cadre (HTML code) */
    String backgroundColor = "#FFBB33"
    /** Type de remplissement du cadre (PLAIN, IMAGE, ...) */
    String backgroundType = "PLAIN"

    // TODO meilleure endroit pour les valeurs par défaut que ci dessus ?

    long reportedAsSpam
    long reportedAsThreat
    long reportedAsInappropriate

    static belongsTo = User
    static hasMany = [receivedBy: Spread, ignoredBy: User, spreadBy: Spread, reports: Report]

    static constraints = {
        author(nullable: false)
        text(nullable: false)
        textColor(nullable: false)
        backgroundType(nullable: false)
    }

    def incrementReportType(ReportType type) {
        switch (type) {
            case ReportType.SPAM:
                reportedAsSpam++
                break;
            case ReportType.INAPPROPRIATE:
                reportedAsInappropriate++
                break;
            case ReportType.THREAT:
                reportedAsThreat++
                break;
        }
    }

    def getMainReportType() {
        if ((reportedAsSpam + reportedAsThreat + reportedAsInappropriate) == 0) {
            return null
        }
        if (reportedAsSpam >= reportedAsThreat && reportedAsSpam >= reportedAsInappropriate) {
            return ReportType.SPAM
        }
        if (reportedAsThreat >= reportedAsSpam && reportedAsThreat >= reportedAsInappropriate) {
            return ReportType.THREAT
        }
        if (reportedAsInappropriate >= reportedAsSpam && reportedAsInappropriate >= reportedAsThreat) {
            return ReportType.INAPPROPRIATE
        }
        return null
    }

    def isReported() {
        return reportedAsSpam != 0 || reportedAsThreat != 0 || reportedAsInappropriate != 0
    }

    def isUserAllowedToRead(User user){
        return author.id == user.id || receivedBy.contains(new Spread(user)) || spreadBy.contains(new Spread(user)) || user.isModerator()
    }

    def isUserAllowedToDelete(User user){
        return author.id == user.id || user.isModerator()
    }

    /**
     * Par sécurité : pour ne pas autoriser l'envoi dans le message json de ces champs
     * TODO à supprimer lorsque le mapping aura été mis en place USPREAD-48
     * @return
     */
    def clearForCreation(){
        receivedBy = new HashSet<Spread>()
        ignoredBy = new HashSet<User>()
        spreadBy = new HashSet<Spread>()
        reports = new HashSet<Report>()
        nbSpread = 0
        id = null
    }

    /**
     * Recherche de la date de réception du message pour l'utilisateur donné
     * @param user Un utilisateur
     * @return Date de réception ou null si non reçus par l'utilisateur
     */
    Date getDateReception(User user) {
        Spread spread = receivedBy.find({Spread s -> s.user == user})
        if (spread != null) {
            return spread.date
        }
        return null
    }

    /**
     * Recherche de la date de propagation du message pour l'utilisateur donné
     * @param user Un utilisateur
     * @return Date de propagation ou null si non reçus par l'utilisateur
     */
    Date getDateSpread(User user) {
        Spread spread = spreadBy.find({Spread s -> s.user == user})
        if (spread != null) {
            return spread.date
        }
        return null
    }

    String toString(){
        if (null != text){
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
