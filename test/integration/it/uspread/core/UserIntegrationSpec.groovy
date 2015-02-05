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
        def dude = new User(username: 'dude', password: 'dude', email: "dudeNoris@42.fr")
        def yop = new User(username: 'yop', password: 'yop', email: "yop@free.fr")

        def messageNotSent = new Message(text: "Un message de dude")
        dude.addToMessages(messageNotSent)

        def messageSent = new Message(text: "Un message de dude", receivedBy: [new Reception(yop)])
        dude.addToMessages(messageSent)

        def messageSpread = new Message(text: "Un autre de dude", spreadBy: [new Spread(yop)])
        dude.addToMessages(messageSpread)

        def messageReported = new Message(text: "Un autre de dude salace", reports: [new Report(yop, ReportType.SPAM)])
        dude.addToMessages(messageReported)


        def receivedByDude = new Message(text: "Un message de yop", receivedBy: [new Reception(dude)])
        yop.addToMessages(receivedByDude)

        def spreadByDude = new Message(text: "Un autre de yop", spreadBy: [new Spread(dude)])
        yop.addToMessages(spreadByDude)

        def reportedByDude = new Message(text: "Un autre de yop", reports: [new Report(dude, ReportType.SPAM)])
        yop.addToMessages(reportedByDude)

        when: "the user is saved"
        dude.save(flush: true)
        yop.save(flush: true)

        then: "so is the message"
        Message.exists(messageNotSent.id)
        Message.exists(messageSent.id)
        Message.exists(messageSpread.id)
        Message.exists(messageReported.id)
        Message.exists(receivedByDude.id)
        Message.exists(spreadByDude.id)
        Message.exists(reportedByDude.id)
    }

    void "Deleting a user deletes the messages he wrote"() {
        given: "An existing user and his message"
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com')
        def jim = new User(username: 'jim', password: 'jim', email: 'jim@gmail.com')
        joe.save(flush: true)
        jim.save(flush: true)

        def messageDeJoeAJim = new Message(text: 'hello', receivedBy: [new Reception(jim)])
        joe.addToMessages(messageDeJoeAJim)

        def messageDeJoePropageParJim = new Message(text: 'hello', spreadBy: [new Spread(jim)])
        joe.addToMessages(messageDeJoePropageParJim)

        def messageDeJoeSignaleParJim = new Message(text: 'hello', reports: [new Report(jim, ReportType.SPAM)])
        joe.addToMessages(messageDeJoeSignaleParJim)

        def messageDeJimAJoe = new Message(text: "hello", receivedBy: [new Reception(joe)])
        jim.addToMessages(messageDeJimAJoe)

        def messageDeJimPropageParJoe = new Message(text: "hello", spreadBy: [new Spread(joe)])
        jim.addToMessages(messageDeJimPropageParJoe)

        def messageDeJimSignaleParJoe = new Message(text: "hello", reports: [new Report(joe, ReportType.SPAM)])
        jim.addToMessages(messageDeJimSignaleParJoe)

        joe.save(flush: true)
        jim.save(flush: true)
        when: "the message is deleted"
        Message.createCriteria().list {
            receivedBy {
                eq('user.id', joe.id)
            }
        }.each {
            ((Message)it).removeFromReceivedBy(((Message)it).receivedBy.find { r -> r.user == joe})
        }

        Message.createCriteria().list {
            reports {
                eq('reporter.id', joe.id)
            }
        }.each {
            ((Message)it).removeFromReports(new Report(joe))
        }

        Report.createCriteria().list {
            reporter {
                eq('id', joe.id)
            }
        }.each{ ((Report) it).delete(flush: true) }

        Message.createCriteria().list {
            spreadBy {
                eq('user.id', joe.id)
            }
        }.each {
            ((Message)it).removeFromSpreadBy(((Message)it).spreadBy.find { r -> r.user == joe})
        }
        joe.delete(flush: true)

        then: "The Message is also deleted"
        joe.id != null
        messageDeJoeAJim.id != null
        messageDeJoeSignaleParJim.id != null
        messageDeJoePropageParJim.id != null
        !User.exists(joe.id)
        !Message.exists(messageDeJoeAJim.id)
        !Message.exists(messageDeJoeSignaleParJim.id)
        !Message.exists(messageDeJoePropageParJim.id)

        User.exists(jim.id)
        Message.exists(messageDeJimAJoe.id)
        Message.exists(messageDeJimSignaleParJoe.id)
        Message.exists(messageDeJimPropageParJoe.id)
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

        def message = new Message(text: 'hello jim', receivedBy: [new Reception(jim)])
        joe.addToMessages(message)

        when: "jim looks at his inbox"
        def inbox = Message.createCriteria().list {
            receivedBy {
                eq('user.id', jim.id)
            }
        }

        then: "it's joe's message"
        def messageReceived = inbox.get(0)
        messageReceived.author == joe
        messageReceived.id == message.id
    }
}
