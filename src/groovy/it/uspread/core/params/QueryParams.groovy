package it.uspread.core.params

import java.text.SimpleDateFormat

/**
 * Choses utiles pour les paramètre d'entrée des requetes rest
 */
class QueryParams {

    public static final String MESSAGE_RECEIVED = "RECEIVED"
    public static final String MESSAGE_WRITED = "AUTHOR"
    public static final String MESSAGE_SPREAD = "SPREAD"

    public static final String AFTER_OR_EQUALS = "after"
    public static final String BEFORE_OR_EQUALS = "before"

    /** Formattage utilisé pour les dates paramètre de requete (date, heure, minute, milliseconde et timezone) */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSSZ")
}
