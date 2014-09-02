package it.uspread.core



import spock.lang.*

/**
 *
 */
class UserIntegrationSpec extends Specification {

    void "A user creates a message"(){
        given:"A user and his message"
        def joe = new User(email: 'joe@gmail.com')
        def message = new Message(text: 'hello')
        joe.addToMessages(message)

        when:"the user is saved"
        joe.save(flush: true)

        then:"the message is saved too"
        message.id != null
        joe.messages.size() == 1
        message.author.email == joe.email
    }

    void "A deleted user have his messages also deleted"() {
        given:"An existing user and his message"
        def joe = new User(email: 'joe@gmail.com')
        def message = new Message(text: 'hello')
        joe.addToMessages(message)
        joe.save(flush: true)

        when:"the user is deleted"
        joe.delete(flush: true)

        then:"so is the message"
        !Message.exists(message.id)
    }

    void "Deleting a message don't delete the author"() {
        given:"An existing user and his message"
        def joe = new User(email: 'joe@gmail.com')
        def message = new Message(text: 'hello')
        joe.addToMessages(message)
        joe.save(flush: true)

        when:"the message is deleted"
        joe.removeFromMessages(message)

        then:"The user is still there, not the message"
        User.exists(joe.id)
        !Message.exists(message.id)
    }

    void "List messages joe sent to jim"(){
        given: "a message sent from joe to jim"
        // joe
        def joe = new User(email: 'joe@gmail.com')
        joe.save(flush: true)
        def message = new Message(text: 'hello jim')
        joe.addToMessages(message)
        // jim
        def jim = new User(email: 'jim@gmail.com')
        jim.save(flush: true)
        message.addToSentTo(jim)

        when: "jim looks at his inbox"
        def inbox = Message.createCriteria().list {
            sentTo{
                eq('id', jim.id)
            }
        }

        then: "it's joe's message"
        def messageReceived = inbox.get(0)
        messageReceived.author == joe
        messageReceived.id == message.id
    }
}
