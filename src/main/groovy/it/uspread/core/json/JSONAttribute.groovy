package it.uspread.core.json

/**
 * Description des attributs JSON que l'on utilise en input et output JSON<br>
 * Utiser des noms pas trop long pour optimiser le trafic réseaux
 */
class JSONAttribute {
    public static final String USER_ID = "id"
    public static final String USER_USERNAME = "usr"
    public static final String USER_PASSWORD = "pass"
    public static final String USER_EMAIL = "email"
    public static final String USER_ROLE = "role"
    public static final String USER_PREFLANGUAGE = "lang"
    public static final String USER_LOCATION = "loc"
    public static final String USER_MESSAGELOCATED = "msgLoc"
    public static final String USER_REPORTSSENT = "reportsSent"
    public static final String USER_REPORTSRECEIVED = "reportsReceived"
    public static final String USER_MODERATIONREQUIRED = "moderationRequired"
    public static final String USER_PUSHTOKEN = "pToken"
    public static final String USER_DEVICE = "device"

    public static final String MESSAGE_ID = "id"
    public static final String MESSAGE_TYPE = "type"
    public static final String MESSAGE_AUTHOR = "author"
    public static final String MESSAGE_DATECREATED = "created"
    public static final String MESSAGE_DATERECEIVED = "received"
    public static final String MESSAGE_DATESPREAD = "spread"
    public static final String MESSAGE_NBSPREAD = "nbSpread"
    public static final String MESSAGE_TEXT = "txt"
    public static final String MESSAGE_TEXTCOLOR = "txtColor"
    public static final String MESSAGE_BACKGROUNDTYPE = "bgType"
    public static final String MESSAGE_BACKGROUNDCOLOR = "bgColor"
    public static final String MESSAGE_BACKGROUNDIMAGE = "img"
    public static final String MESSAGE_LANGUAGE = "lang"
    public static final String MESSAGE_LINK = "link"
    public static final String MESSAGE_LOCATION = "loc"
    public static final String MESSAGE_MAINREPORTTYPE = "mainReportType"

    public static final String STATUS_WORLDQUOTAREACHED = "wQuota"
    public static final String STATUS_LOCALQUOTAREACHED = "lQuota"
    public static final String STATUS_NBMESSAGEWRITED = "msgWrited"
    public static final String STATUS_NBMESSAGESPREAD = "msgSpread"
    public static final String STATUS_NBMESSAGEIGNORED = "msgIgnored"
    public static final String STATUS_NBMESSAGEREPORTED = "msgReported"
}
