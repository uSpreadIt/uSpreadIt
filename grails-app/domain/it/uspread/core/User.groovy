package it.uspread.core

/**
 * Modèle de l'utilisateur
 */
class User {

    transient springSecurityService

    String username
    String password
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    String email
    Date lastReceivedMessageDate
    boolean specialUser
    long score
    long reportsSent
    long reportsReceived

    String iosPushToken

    static hasMany = [messages: Message, androidPushTokens: String]
    static mappedBy = [messages: 'author']

    static transients = ['springSecurityService']

    static constraints = {
        username blank: false, unique: true
        password blank: false
        email(unique: true, email: true, blank: false)
        lastReceivedMessageDate nullable: true
        iosPushToken nullable: true
    }

    static mapping = {
        table 'author'
        password column: '`password`'
        messages cascade: 'all-delete-orphan'
    }

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role }
    }

    def beforeInsert() {
        encodePassword()
    }

    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    protected void encodePassword() {
        password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
    }

    def isModerator(){
        getAuthorities().any { it.authority == Role.ROLE_MODERATOR }
    }

    /**
     * Indique qu'une action de modération est nécessaire pour l'utilisateur<br>
     * Messages en attente de modération, signalement zélé...
     * @return
     */
    def isModerationRequired() {
        return messages.any() { Message msg -> msg.isReported()}
    }

    String toString(){
        return null != email ? email : "<EMPTY>"
    }
}
