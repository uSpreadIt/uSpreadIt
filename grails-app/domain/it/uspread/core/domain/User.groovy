package it.uspread.core.domain

/**
 * Modèle de l'utilisateur
 */
class User implements Serializable {

    private static final long serialVersionUID = 1

    /** Nombre de caractère maximal d'un nom d'utilisateur */
    public static final int USERNAME_MAX_LENGTH = 20
    /** Nombre de caractère minimal d'un mot de passe */
    public static final int PASSWORD_MIN_LENGTH = 8
    /** Nombre de caractère maximal d'un mot de passe */
    public static final int PASSWORD_MAX_LENGTH = 64

    transient springSecurityService

    /** Spring Security attribute */
    boolean enabled = true
    /** Spring Security attribute */
    boolean accountExpired
    /** Spring Security attribute */
    boolean accountLocked
    /** Spring Security attribute */
    boolean passwordExpired

    /** Pseudo (Spring Security attribute) */
    String username
    /** Mot de passe (Spring Security attribute) */
    String password
    /** Email */
    String email

    /** Dernière date de réception d'un message */
    Date lastReceivedMessageDate

    /** Nombre de signalement effectué */
    long reportsSent
    /** Nombre de signalement reçus */
    long reportsReceived

    /** Score TODO de la conception à faire */
    long score

    /** Indique que l'utilisateur n'est pas un simple utilisateur */
    boolean specialUser

    /** La liste des messages écrits par l'utilisateur */
    Set<Message> messages
    /** Tokens du système de push Android (Correspond au périphérique sur lesquel l'user est connecté) */
    Set<String> androidPushTokens
    /** Tokens du système de push ios (Correspond au périphérique sur lesquel l'user est connecté) */
    Set<String> iosPushTokens

    static transients = ['springSecurityService']

    static hasMany = [messages: Message, androidPushTokens: String, iosPushTokens: String]

    static mappedBy = [messages: 'author']

    static mapping = {
        version(true)
        table('`user`') // PostegreSQL a réservé 'user' donc on échappe
        id([generator: 'sequence', params: [sequence:'user_sequence']])
        username(length: USERNAME_MAX_LENGTH)
        password(column: '`password`', length: PASSWORD_MAX_LENGTH)
        messages(cascade: 'all-delete-orphan')
        androidPushTokens(cascade: 'all-delete-orphan', joinTable: [column: 'android_push_token'])
        iosPushTokens(cascade: 'all-delete-orphan', joinTable: [column: 'ios_push_token'])
    }

    static constraints = {
        username(blank: false, unique: true, maxsize: USERNAME_MAX_LENGTH)
        password(blank: false, maxsize: PASSWORD_MAX_LENGTH) // TODO appliquer le nombre de caractere minimum quand ce sera pas chiant pour nous
        email(blank: false, unique: true, email: true)
        lastReceivedMessageDate(nullable: true)
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

    /**
     * Retourne les rôles que possède l'utilisateur (Nécessaire pour spring security : soit ce simple getter soit la définition d'un many-to-many authorities)
     * @return
     */
    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect({ it.role })
    }

    /**
     * Indique si l'utilisateur est un modérateur
     * @return
     */
    boolean isModerator(){
        getAuthorities().any({ it.authority == Role.ROLE_MODERATOR })
    }

    /**
     * Indique si l'utilisateur est un administrateur
     * @return
     */
    boolean isAdministrator(){
        getAuthorities().any({ it.authority == Role.ROLE_ADMINISTRATOR })
    }

    /**
     * Indique qu'une action de modération est nécessaire pour l'utilisateur<br>
     * TODO conception a faire (trop de contenu modéré ou trop de fausse modération ?)
     * @return
     */
    boolean isModerationRequired() {
        messages?.any({ it.isReported()})
    }

    @Override
    int hashCode() {
       username?.hashCode() ?: 0
    }

    @Override
    boolean equals(Object other) {
       is(other) || (other instanceof User && other.username == username)
    }

    @Override
    String toString() {
       username
    }
}
