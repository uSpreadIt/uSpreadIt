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
