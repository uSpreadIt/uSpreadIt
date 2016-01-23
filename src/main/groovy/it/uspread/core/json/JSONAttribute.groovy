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
    public static final String USER_REPORTSSENT = "reportsSent"
    public static final String USER_REPORTSRECEIVED = "reportsReceived"
    public static final String USER_MODERATIONREQUIRED = "moderationRequired"
    public static final String USER_PUSHTOKEN = "pushToken"
    public static final String USER_DEVICE = "device"

    public static final String MESSAGE_ID = "id"
    public static final String MESSAGE_AUTHOR = "author"
    public static final String MESSAGE_DATECREATED = "dateCreated"
    public static final String MESSAGE_DATERECEIVED = "dateReceived"
    public static final String MESSAGE_DATESPREAD = "dateSpread"
    public static final String MESSAGE_NBSPREAD = "nbSpread"
    public static final String MESSAGE_TEXT = "txt"
    public static final String MESSAGE_TEXTCOLOR = "txtColor"
    public static final String MESSAGE_BACKGROUNDTYPE = "bgType"
    public static final String MESSAGE_BACKGROUNDCOLOR = "bgColor"
    public static final String MESSAGE_BACKGROUNDIMAGE = "img"
    public static final String MESSAGE_LINK = "link"
    public static final String MESSAGE_TYPE = "type"
    public static final String MESSAGE_LOCATION = "location"
    public static final String MESSAGE_MAINREPORTTYPE = "mainReportType"

    public static final String STATUS_WORLDQUOTAREACHED = "worldQuotaReached"
    public static final String STATUS_LOCALQUOTAREACHED = "localQuotaReached"
    public static final String STATUS_NBMESSAGEWRITED = "nbMsgWrited"
    public static final String STATUS_NBMESSAGESPREAD = "nbMsgSpread"
    public static final String STATUS_NBMESSAGEIGNORED = "nbMsgIgnored"
    public static final String STATUS_NBMESSAGEREPORTED = "nbMsgReported"
}
