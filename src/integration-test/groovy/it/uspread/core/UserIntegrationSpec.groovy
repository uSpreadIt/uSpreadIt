package it.uspread.core

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import it.uspread.core.domain.Message
import it.uspread.core.domain.Report
import it.uspread.core.domain.Spread
import it.uspread.core.domain.User
import it.uspread.core.type.BackgroundType
import it.uspread.core.type.Language
import it.uspread.core.type.MessageType
import it.uspread.core.type.ReportType
import spock.lang.*

/**
 * Test utile pour mettre en valeur les problèmes éventuelle avec le modèle de donnée et hibernate
 */
@Integration
@Rollback
class UserIntegrationSpec extends Specification {

    def userService
    def messageService

    private Message createNewPlainMessage(Map param) {
        param['backgroundType'] = BackgroundType.PLAIN
        param['backgroundColor'] = 'FFBB33'
        param['textColor'] = '000000'
        param['type'] = MessageType.WORLD
        return new Message(param)
    }

    void "A user creates a message"() {
        given: "A user and his message"
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        def message = createNewPlainMessage(text: 'hello')
        joe.addToMessages(message)

        when: "the user is saved"
        joe.save([flush: true])

        then: "the message is saved too"
        message.id != null
        joe.messages.size() == 1
        message.author == joe
    }

    void "Test lots of messages creations"() {
        given: "Existing users and their messages"
        def dude = new User(username: 'dude', password: 'dude', email: "dudeNoris@42.fr", preferredLanguage: Language.FR, publicUser: true)
        def yop = new User(username: 'yop', password: 'yop', email: "yop@free.fr", preferredLanguage: Language.FR, publicUser: true)
        dude.save(flush: true)
        yop.save(flush: true)

        def messageNotSent = createNewPlainMessage(text: "Un message de dude")
        dude.addToMessages(messageNotSent)

        def receivedByYop = createNewPlainMessage(text: "Un autre message de dude")
        receivedByYop.addToReceivedBy(new Spread(yop))
        dude.addToMessages(receivedByYop)

        def spreadByYop = createNewPlainMessage(text: "Encore un autre de dude")
        spreadByYop.addToSpreadBy(new Spread(yop))
        dude.addToMessages(spreadByYop)

        def ignoredByYop = createNewPlainMessage(text: "un message pourri de dude")
        ignoredByYop.addToIgnoredBy(yop)
        dude.addToMessages(ignoredByYop)

        def reportedByYop = createNewPlainMessage(text: "Un spam de dude")
        reportedByYop.addToReports( new Report(yop, ReportType.SPAM))
        dude.addToMessages(reportedByYop)

        def receivedByDude = createNewPlainMessage(text: "Un message de yop")
        receivedByDude.addToReceivedBy(new Spread(dude))
        yop.addToMessages(receivedByDude)

        def spreadByDude = createNewPlainMessage(text: "Un autre de yop")
        spreadByDude.addToSpreadBy(new Spread(dude))
        yop.addToMessages(spreadByDude)

        def ignoredByDude = createNewPlainMessage(text: "un message pourri de yop")
        ignoredByDude.addToIgnoredBy(dude)
        yop.addToMessages(ignoredByDude)

        def reportedByDude = createNewPlainMessage(text: "un spam de yop")
        reportedByDude.addToReports( new Report(dude, ReportType.SPAM))
        yop.addToMessages(reportedByDude)

        // TODO ajouter création message avec image et avec link

        when: "users are saved"
        dude.save([flush: true])
        yop.save([flush: true])

        then: "so messages are saved too"
        Message.exists(messageNotSent.id)
        Message.exists(receivedByYop.id)
        Message.exists(spreadByYop.id)
        Message.exists(ignoredByYop.id)
        Message.exists(reportedByYop.id)
        Message.exists(receivedByDude.id)
        Message.exists(spreadByDude.id)
        Message.exists(ignoredByDude.id)
        Message.exists(reportedByDude.id)
    }

    void "List messages writed by dude (And saved by message.save action)"() {
        given: "An existing user and his message"
        def dude = new User(username: 'dude', password: 'dude', email: "dudeNoris@42.fr", preferredLanguage: Language.FR, publicUser: true)
        dude.save(flush: true)

        def messageNotSent = createNewPlainMessage(text: "Un message de dude")
        messageNotSent.author = dude

        when: "the message is saved"
        messageNotSent.save([flush: true])
        dude.discard() // dude ne doit plus être dans la session afin qu'il soit rechargé

        then: "so user own this message"
        Message.exists(messageNotSent.id)
        User.get(dude.id).messages.size() == 1
    }

    void "List messages joe sent to jim"() {
        given: "a message sent from joe to jim"
        // joe
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        joe.save(flush: true)
        // jim
        def jim = new User(username: 'jim', password: 'jim', email: 'jim@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        jim.save(flush: true)

        def message = createNewPlainMessage(text: 'hello jim')
        message.addToReceivedBy(new Spread(jim))
        joe.addToMessages(message)

        when: "jim looks at his inbox"
        def inbox = Message.createCriteria().list {
            receivedBy {
                eq('user.id', jim.id)
            }
        }

        then: "it's joe's message"
        inbox.size() == 1
        def messageReceived = inbox.get(0)
        messageReceived.author == joe
        messageReceived.id == message.id
    }

    void "List messages spread by jim"() {
        given: "a message from joe spread by jim"
        // joe
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        joe.save(flush: true)
        // jim
        def jim = new User(username: 'jim', password: 'jim', email: 'jim@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        jim.save(flush: true)

        def message = createNewPlainMessage(text: 'hello jim')
        message.addToSpreadBy(new Spread(jim))
        joe.addToMessages(message)

        when: "jim looks at his spread box"
        def inbox = Message.createCriteria().list {
            spreadBy {
                eq('user.id', jim.id)
            }
        }

        then: "it's joe's message"
        inbox.size() == 1
        def messageSpread = inbox.get(0)
        messageSpread.author == joe
        messageSpread.id == message.id
    }

    void "List messages ignored by jim"() {
        given: "a message from joe ignored by jim"
        // joe
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        joe.save(flush: true)
        // jim
        def jim = new User(username: 'jim', password: 'jim', email: 'jim@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        jim.save(flush: true)

        def message = createNewPlainMessage(text: 'hello jim')
        message.addToIgnoredBy(jim)
        joe.addToMessages(message)

        when: "jim looks at his ignored message"
        def inbox = Message.createCriteria().list {
            ignoredBy {
                eq('id', jim.id)
            }
        }

        then: "it's joe's message"
        inbox.size() == 1
        def messageIgnored = inbox.get(0)
        messageIgnored.author == joe
        messageIgnored.id == message.id
    }

    void "List messages reported by jim"() {
        given: "a message from joe reported by jim"
        // joe
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        joe.save(flush: true)
        // jim
        def jim = new User(username: 'jim', password: 'jim', email: 'jim@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        jim.save(flush: true)

        def message = createNewPlainMessage(text: 'hello jim')
        message.addToReports(new Report(jim, ReportType.SPAM))
        joe.addToMessages(message)

        when: "jim looks at his reported message"
        def inbox = Message.createCriteria().list {
            reports {
                eq('reporter.id', jim.id)
            }
        }

        then: "it's joe's message"
        inbox.size() == 1
        def messageReported = inbox.get(0)
        messageReported.author == joe
        messageReported.id == message.id
    }

    void "Deleting a user deletes the messages he wrote"() {
        given: "An existing user and his message"
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        def jim = new User(username: 'jim', password: 'jim', email: 'jim@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        joe.save(flush: true)
        jim.save(flush: true)

        def messageDeJoeAJim = createNewPlainMessage(text: 'hello')
        messageDeJoeAJim.addToReceivedBy(new Spread(jim))
        joe.addToMessages(messageDeJoeAJim)
        def messageDeJoePropageParJim = createNewPlainMessage(text: 'hello')
        messageDeJoePropageParJim.addToSpreadBy(new Spread(jim))
        joe.addToMessages(messageDeJoePropageParJim)
        def messageDeJoeIgnoreParJim = createNewPlainMessage(text: 'hello')
        messageDeJoeIgnoreParJim.addToIgnoredBy(jim)
        joe.addToMessages(messageDeJoeIgnoreParJim)

        def messageDeJoeSignaleParJim = createNewPlainMessage(text: 'hello')
        messageDeJoeSignaleParJim.addToReports( new Report(jim, ReportType.SPAM))
        joe.addToMessages(messageDeJoeSignaleParJim)

        def messageDeJimAJoe = createNewPlainMessage(text: "hello")
        messageDeJimAJoe.addToReceivedBy(new Spread(joe))
        jim.addToMessages(messageDeJimAJoe)
        def messageDeJimPropageParJoe = createNewPlainMessage(text: "hello")
        messageDeJimPropageParJoe.addToSpreadBy(new Spread(joe))
        jim.addToMessages(messageDeJimPropageParJoe)
        def messageDeJimIgnoreParJoe = createNewPlainMessage(text: "hello")
        messageDeJimIgnoreParJoe.addToIgnoredBy(joe)
        jim.addToMessages(messageDeJimIgnoreParJoe)

        def messageDeJimSignaleParJoe = createNewPlainMessage(text: "hello")
        messageDeJimSignaleParJoe.addToReports(new Report(joe, ReportType.SPAM))
        jim.addToMessages(messageDeJimSignaleParJoe)

        joe.save(flush: true)
        jim.save(flush: true)

        when: "the user is deleted"
        userService.deleteUser(joe)

        then: "his messages is also deleted"
        joe.id != null
        messageDeJoeAJim.id != null
        messageDeJoePropageParJim.id != null
        messageDeJoeIgnoreParJim.id != null
        messageDeJoeSignaleParJim.id != null
        !User.exists(joe.id)
        !Message.exists(messageDeJoeAJim.id)
        !Message.exists(messageDeJoePropageParJim.id)
        !Message.exists(messageDeJoeIgnoreParJim.id)
        !Message.exists(messageDeJoeSignaleParJim.id)

        User.exists(jim.id)
        Message.exists(messageDeJimAJoe.id)
        Message.exists(messageDeJimPropageParJoe.id)
        Message.exists(messageDeJimIgnoreParJoe.id)
        Message.exists(messageDeJimSignaleParJoe.id)
        Spread.where({ user == joe }).count() == 0
        Report.where({ reporter == joe }).count() == 0
    }

    void "Deleting a message don't delete the author 1"() {
        given: "An existing user and his message"
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        def message = createNewPlainMessage(text: 'hello')
        joe.addToMessages(message)
        joe.save(flush: true)

        when: "the message is deleted"
        joe.removeFromMessages(message)
        joe.save(flush: true)

        then: "The user is still there, not the message"
        User.exists(joe.id)
        !Message.exists(message.id)
    }

    void "Deleting a message don't delete the author 2"() {
        given: "An existing user and his message"
        def joe = new User(username: 'joe', password: 'joe', email: 'joe@gmail.com', preferredLanguage: Language.FR, publicUser: true)
        def message = createNewPlainMessage(text: 'hello')
        joe.addToMessages(message)
        joe.save(flush: true)

        when: "the message is deleted"
        joe.discard() // joe ne doit pas être dans la session pour pouvoir directement supprimer un message
        message.delete(flush: true)

        then: "The user is still there, not the message"
        User.exists(joe.id)
        !Message.exists(message.id)
    }
}
