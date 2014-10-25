package it.uspread.core

/**
 * Modèle du message
 */
class Message {

	/** Auteur */
	User author
	/** Nombre de propagation */
	long nbSpread
	/** Date de création TODO gérer la dé/sérialization pour correspondre en java à ceci : "yyyy-MM-dd HH:mm:ssZ" : 2014-05-15 10:00:00-0800 */
	Date dateCreated
	/** Texte du message */
	String text
	/** Couleur du texte (HTML code) */
	String textColor = "#000000"
	/** Couleur de fond du cadre (HTML code) */
	String backgroundColor = "#FFBB33"
	/** Type de remplissement du cadre TODO Rempalcer par une enum ? */
	String backgroundType = "PLAIN"

	// TODO meilleure endroit pour les valeurs par défaut que ci dessus ?

    static belongsTo = User
    static hasMany = [sentTo: User, ignoredBy: User, spreadBy: User, reportedBy: User]

	static constraints = {
        author(nullable: false)
		text(nullable: false)
		textColor(nullable: false)
		backgroundType(nullable: false)
	}

    def isUserAllowedToRead(User user){
        return author.id == user.id || sentTo.contains(user) || spreadBy.contains(user) || user.isModerator()
    }

    def isUserAllowedToDelete(User user){
        return author.id == user.id || user.isModerator()
    }

    /**
     * Par sécurité : pour ne pas autoriser l'envoi dans le message json de ces champs
     * TODO à supprimer lorsque le mapping aura été mis en place USPREAD-28
     * @return
     */
    def clearForCreation(){
        sentTo = new HashSet<User>()
        ignoredBy = new HashSet<User>()
        spreadBy = new HashSet<User>()
        reportedBy = new HashSet<User>()
        nbSpread = 0
        id = null
    }

    String toString(){
        if (null != text){
            if (30 >= text.size()){
                return text
            } else {
                return text.substring(0,30) + "..."
            }
        } else {
            return "<EMPTY>"
        }
    }
}
