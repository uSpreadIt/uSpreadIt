package it.uspread.core

class User {

    String email

    static hasMany = [messages: Message]
    static mappedBy = [messages: 'author']

    static mapping = {
        messages cascade: 'all-delete-orphan'
    }

    static constraints = {
        email(unique: true, email: true)
    }
}
