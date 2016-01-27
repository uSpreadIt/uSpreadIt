package it.uspread.core.params

/**
 * Nom des paramètres d'entrée des requetes rest<br>
 * Utiser des noms pas trop long pour optimiser le trafic réseaux si le paramêtre est beaucoup utilisé
 */
class URLParamsName {

    public static final String ID = "id"

    public static final String MESSAGE_ID = "messageId"
    public static final String MESSAGE_ASKED = "msg"
    public static final String MESSAGE_COUNT = "nb"
    public static final String MESSAGE_DATE = "date"
    public static final String MESSAGE_OPERATOR = "op"
    public static final String MESSAGE_ONLY_DYNAMICVALUE = "onlyDyn"
    public static final String MESSAGE_ONLY_IMAGE = "onlyImg"
    public static final String MESSAGE_REPORTTYPE = "type"

    public static final String USER_ID = "userId"
    public static final String USER_USERNAME = "usr"
    public static final String USER_EMAIL = "email"
    public static final String USER_ROLE = "role"
    public static final String USER_MODERATIONREQUIRED = "moderationRequired"
    public static final String USER_PUSHTOKEN = "pToken"
    public static final String USER_DEVICE = "device"
    public static final String USER_ONLY_QUOTA = "onlyQuota"
}
