package it.uspread.core.domain

import it.uspread.core.type.Language

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
    /** Nombre de caractère maximal d'un email */
    public static final int EMAIL_MAX_LENGTH = 40
    /** Nombre de caractère maximal d'une geolocalisation */
    public static final int LOCATION_MAX_LENGTH = 25

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

    /** Date de création de l'utilisateur */
    Date dateCreated // Ne pas renommer car ce nom est un nom spécial détécté par Grails cf autoTimestamp
    /** Indique si le compte utilisateur a été vérifié (validation de l'email donné) */
    boolean accountVerified

    /** Langue préféré de l'utilisateur */
    Language preferredLanguage

    /** Indique si l'utilisateur est géolocalisé (Présence pour optimisation) */
    boolean located
    /** Géolocalisation fixé par l'utilisateur (latitude,longitude) */
    String location
    /** Indique si l'utilisateur accepte de géolocalisé ses messages */
    boolean messageLocated

    /** Nombre de signalement effectué */
    long reportsSent
    /** Nombre de signalement reçus */
    long reportsReceived

    /** Dernière date de réception d'un message (Présence pour optimisation) */
    Date lastReceivedMessageDate

    /** Score TODO de la conception à faire */
    long score

    /** Indique que l'utilisateur est "Premium" */
    boolean premiumUser

    /** Indique que l'utilisateur est un utilisateur public (Présence pour optimisation) */
    boolean publicUser

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
        table('`user`') // PostegreSQL a réservé le mot clé 'user' donc on échappe
        id([generator: 'sequence', params: [sequence:'user_sequence']])
        username(length: USERNAME_MAX_LENGTH)
        password(column: '`password`', length: PASSWORD_MAX_LENGTH) // 'password' reservé en PostegreSQL donc on échappe
        email(length: EMAIL_MAX_LENGTH)
        preferredLanguage(enumType: 'string', length: 2, index: 'idx_user_lang')
        located(index: 'idx_user_located')
        location(length: LOCATION_MAX_LENGTH)
        messageLocated(index: 'idx_user_msg_located')
        premiumUser(index: 'idx_user_premium')
        publicUser(updateable: false, index: 'idx_user_public') // Pour s'assurer que au dela de la création cet élément ne puisse plus être modifié
        messages(cascade: 'all-delete-orphan')
        androidPushTokens(cascade: 'all-delete-orphan', joinTable: [column: 'android_push_token'])
        iosPushTokens(cascade: 'all-delete-orphan', joinTable: [column: 'ios_push_token'])
    }

    static constraints = {
        username(blank: false, unique: true, maxsize: USERNAME_MAX_LENGTH)
        password(blank: false, maxsize: PASSWORD_MAX_LENGTH) // TODO appliquer le nombre de caractere minimum quand ce sera pas chiant pour nous
        email(blank: false, unique: true, email: true, maxsize: EMAIL_MAX_LENGTH)
        preferredLanguage(nullable: true, validator: { val, obj ->
            // Autorisé une langue préfére null que si ce n'est pas un user public
            return obj.publicUser && val != null ||  !obj.publicUser
        })
        location(nullable: true, maxsize: LOCATION_MAX_LENGTH, validator: { val, obj ->
            // Autorisé une geolocalisation que si 'located'
            return obj.located && val != null ||  val == null
        })
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
