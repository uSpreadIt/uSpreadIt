package it.uspread.core.domain

import it.uspread.core.type.ReportType

/**
 * Modèle du signalement des messages.
 */
class Report implements Serializable {

    private static final long serialVersionUID = 1L

    /** Message signalé */
    Message message
    /** Utilisateur ayant fait ce signalement */
    User reporter
    /** Type de signalement donné */
    ReportType type

    static belongsTo = Message

    static mapping = {
        version(false)
        id([generator:'sequence', params:[sequence:'report_sequence']])
        message(index: 'message_idx')
        reporter(index: 'reporter_idx')
        type(enumType: 'string')
    }

    static constraints = {
    }

    /**
     * Création d'un nouveau signalement
     * @param reporter Un utilisateur
     * @param type le type de signalement
     */
    Report(User reporter, ReportType type) {
        this.reporter = reporter
        this.type = type
    }

}
