package it.uspread.core.domain


/**
 * Modèle de propagation des messages
 */
class Spread implements Serializable {

    private static final long serialVersionUID = 1L

    /** Message reçus */
    Message messageReceived
    /** Message propagé */
    Message messageSpread
    /** Utilisateur concerné */
    User user
    /** Date de l'action */
    Date date

    static belongsTo = Message

    static mapping = {
        messageReceived(index: 'messageReceived_idx')
        messageSpread(index: 'messageSpread_idx')
        user(index: 'user_idx')
        version(false)
    }

    static constraints = {
        messageReceived(nullable:true)
        messageSpread(nullable:true, validator: { val, obj ->
            return obj.messageReceived != null && val == null ||  obj.messageReceived == null && val != null
        })
    }

    /**
     * Création d'une nouvelle propagation à cet instant
     * @param user l'utilisateur recevant ou propagant le message
     */
    Spread(User user) {
        this.user = user
        this.date = new Date()
    }
}
