package it.uspread.core


import spock.lang.*

/**
 *
 */
class UserIntegrationSpec extends Specification {

    void "A user creates a message"() {
        given: "A user and his message"
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com')
        def message = new Message(text: 'hello')
        joe.addToMessages(message)

        when: "the user is saved"
        joe.save(flush: true)

        then: "the message is saved too"
        message.id != null
        joe.messages.size() == 1
        message.author.email == joe.email
    }

    void "Test message creations"() {
        given: "An existing user and his message"
        def dude = new User(username: 'chuck', password: 'chuck', email: "ChuckNoris@42.fr")
        def yop = new User(username: 'etienne', password: 'etienne', email: "Etienne@free.fr")

        def messageNotSent = new Message(text: "Un message de dude")
        dude.addToMessages(messageNotSent)

        def messageSent = new Message(text: "Un message de dude", sentTo: [yop])
        dude.addToMessages(messageSent)

        def messageSpread = new Message(text: "Un autre de dude", spreadBy: [yop])
        dude.addToMessages(messageSpread)

        def messageReported = new Message(text: "Un autre de dude salace", reportedBy: [yop])
        dude.addToMessages(messageReported)


        def sentToDude = new Message(text: "Un message de yop", sentTo: [dude])
        yop.addToMessages(sentToDude)

        def spreadByDude = new Message(text: "Un autre de yop", spreadBy: [dude])
        yop.addToMessages(spreadByDude)

        def reportedByDude = new Message(text: "Un autre de yop", reportedBy: [dude])
        yop.addToMessages(reportedByDude)

        when: "the user is saved"
        dude.save(flush: true)
        yop.save(flush: true)

        then: "so is the message"
        Message.exists(messageNotSent.id)
        Message.exists(messageSent.id)
        Message.exists(messageSpread.id)
        Message.exists(messageReported.id)
        Message.exists(sentToDude.id)
        Message.exists(spreadByDude.id)
        Message.exists(reportedByDude.id)
    }

    void "A deleted user have his messages also deleted"() {
        given: "An existing user and his message"
        def chuck = new User(username: 'chuck', password: 'chuck', email: "ChuckNoris@42.fr")
        def etienne = new User(username: 'etienne', password: 'etienne', email: "Etienne@free.fr")

        def messageSent = new Message(text: "Un message de chuck", sentTo: [etienne])
        chuck.addToMessages(messageSent)

        def messageSpread = new Message(text: "Un autre de chuck", spreadBy: [etienne])
        chuck.addToMessages(messageSpread)

        def messageReported = new Message(text: "Un autre de chuck salace", reportedBy: [etienne])
        chuck.addToMessages(messageReported)

        chuck.save()

        def sentToChuck = new Message(text: "Un message de Etienne", sentTo: [chuck])
        etienne.addToMessages(sentToChuck)

        def spreadByChuck = new Message(text: "Un autre de Etienne", spreadBy: [chuck])
        etienne.addToMessages(spreadByChuck)

        def reportedByChuck = new Message(text: "Un autre de Etienne", reportedBy: [chuck])
        etienne.addToMessages(reportedByChuck)

        etienne.save()

        when: "the user is deleted"
        chuck.delete(flush: true)

        then: "so is the message"
        !Message.exists(messageSent.id)
        !Message.exists(messageSpread.id)
        !Message.exists(messageReported.id)
        Message.exists(sentToChuck.id)
        Message.exists(spreadByChuck.id)
        Message.exists(reportedByChuck.id)
    }

    void "Deleting a message don't delete the author"() {
        given: "An existing user and his message"
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com')
        def message = new Message(text: 'hello')
        joe.addToMessages(message)
        joe.save(flush: true)

        when: "the message is deleted"
        joe.removeFromMessages(message)

        then: "The user is still there, not the message"
        User.exists(joe.id)
        !Message.exists(message.id)
    }

    void "List messages joe sent to jim"() {
        given: "a message sent from joe to jim"
        // joe
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com')
        joe.save(flush: true)
        // jim
        def jim = new User(username: 'jim', password: 'jim', email: 'jim@gmail.com')
        jim.save(flush: true)

        def message = new Message(text: 'hello jim', sentTo: [jim])
        joe.addToMessages(message)

        when: "jim looks at his inbox"
        def inbox = Message.createCriteria().list {
            sentTo {
                eq('id', jim.id)
            }
        }

        then: "it's joe's message"
        def messageReceived = inbox.get(0)
        messageReceived.author == joe
        messageReceived.id == message.id
    }
}
