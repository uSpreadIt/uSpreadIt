package it.uspread.core.params

import java.text.SimpleDateFormat

/**
 * Choses utiles pour les valeurs des paramètres d'entrée des requetes rest
 */
class QueryParams {

    public static final String MESSAGE_RECEIVED = "RECEIVED"
    public static final String MESSAGE_WRITED = "AUTHOR"
    public static final String MESSAGE_SPREAD = "SPREAD"

    public static final String DEVICE_ANDROID = "ANDROID"
    public static final String DEVICE_IOS = "IOS"

    public static final String OPERATOR_GREATER = "gt"
    public static final String OPERATOR_GREATER_OR_EQUALS = "ge"
    public static final String OPERATOR_LOWER = "lt"

    /** Formattage utilisé pour les dates paramètre de requete (date, heure, minute, milliseconde et timezone) */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSSZ")
}
