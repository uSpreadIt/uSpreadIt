package it.uspread.core

class Message {

	String text
    User author
	long nbSpread

    static belongsTo = User
    static hasMany = [sentTo: User, spreadBy: User, reportedBy: User]

	static constraints = {
        author(nullable: false)
	}

    def isUserAllowedToRead(User user){
        return author.id == user.id || sentTo.contains(user) || spreadBy.contains(user) || user.isModerator()
    }

    def isUserAllowedToDelete(User user){
        return author.id == user.id || user.isModerator()
    }

    /**
     * Par sécurité : pour ne pas autoriser l'envoi dans le message json de ces champs
     * @return
     */
    def clearForCreation(){
        sentTo = new HashSet<User>()
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
