package it.uspread.core

import it.uspread.core.type.BackgroundType

import org.springframework.http.HttpStatus

import spock.lang.Shared
import spock.lang.Specification
import wslite.http.auth.HTTPBasicAuthorization
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.Response

/**
 * Tests qui servent à valider les refactoring important
 * Le serveur doit tourner (en localhost) avant exécution
 * Ne peuvent être rejoués plusieurs fois
 */
class MessageControllerFunctionalSpec extends Specification {

    // TODO couvrir plus de cas un peu comme dans le test functional des user

    private final long idMessageFromUser1 = 1
    private final long idMessage2FromUser1_ToDelete = 2
    private final long idMessageFromUser2 = 3
    private final long idMessage2FromUser2_ToReport = 4
    private final long idMessageFromUser3 = 5

    @Shared def clientModerator = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def clientUser1 = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def clientUser2 = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def clientUser3 = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def clientUser4 = new RESTClient("http://localhost:8080/uSpread-core/rest/")

    def wrapPlainMessage(Map param) {
        param['backgroundType'] = BackgroundType.PLAIN.name()
        param['backgroundColor'] = 'FFBB33'
        param['textColor'] = '000000'
        return param
    }

    private void postMessage(RESTClient client, String text) {
        Response response = client.post([path: "/messages", accept: ContentType.JSON]) {
            type(ContentType.JSON)
            json(wrapPlainMessage([text: text]))
        }

        assert response.statusCode == HttpStatus.CREATED.value
        assert response.json.id != null
    }

    void setup() {
        clientModerator.authorization = new HTTPBasicAuthorization("mod", "mod")
        clientUser1.authorization = new HTTPBasicAuthorization("user1", "user1")
        clientUser2.authorization = new HTTPBasicAuthorization("user2", "user2")
        clientUser3.authorization = new HTTPBasicAuthorization("user3", "user3")
        clientUser4.authorization = new HTTPBasicAuthorization("user4", "user4")

        // TODO lorsqu'on aura mis en place SSL
        // clientModerator.httpClient.sslTrustAllCerts = true
        // clientUser1.httpClient.sslTrustAllCerts = true
        // clientUser2.httpClient.sslTrustAllCerts = true
        // clientUser3.httpClient.sslTrustAllCerts = true
        // clientUser4.httpClient.sslTrustAllCerts = true

        // Créer des messages pour le test
        postMessage(clientUser1, "hello world")
        postMessage(clientUser1, "hello universe")
        postMessage(clientUser2, "he ho calmos")
        postMessage(clientUser2, "Fuck")
        postMessage(clientUser3, "Yo")
    }

    void "user 2 spread"() {
        when: "Message is spread by user2"
        Response response = clientUser2.post([path: "/messages/"+idMessageFromUser1+"/spread", accept: ContentType.JSON]) {
            type(ContentType.JSON)
        }

        then: "Status code is"
        response.statusCode == HttpStatus.ACCEPTED.value
        response.json.id != null
        response.json.nbSpread != null
    }

    void "user 3 ignores"() {
        when: "Message is ignored by user3"
        Response response = clientUser3.post([path: "/messages/"+idMessageFromUser1+"/ignore"]) {
            type(ContentType.JSON)
        }

        then: "Status code is"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "user 4 reports"() {
        when: "Message is reported by user4"
        Response response = clientUser4.post([path: "/messages/"+idMessage2FromUser2_ToReport+"/report?type=SPAM"]) {
            type(ContentType.JSON)
        }

        then: "Status code is"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "user 1 gets his messages"() {
        when: "Messages are got by user1"
        Response response = clientUser1.get([path: "/messages?query=AUTHOR", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json[0] != null
    }

    void "user 1 gets his message"() {
        when: "Message is got by user1"
        Response response = clientUser1.get([path: "/messages/"+idMessageFromUser1, accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.id != null
    }

    void "user 1 deletes a message"() {
        when: "Message is deleted"
        Response response = clientUser1.delete([path: "/messages/"+idMessage2FromUser1_ToDelete])

        then: "Status code is"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "user 1 gets his status"() {
        when: "status is got by user1"
        Response response = clientUser1.get([path: "/users/connected/status", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
    }

    void "mod gets user1 messages"() {
        when: "mod gets user1 messages"
        Response response = clientModerator.get([path: "/users/1/messages", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
    }

    void "mod gets user2 message received"() {
        when: "mod gets user2 message received"
        Response response = clientModerator.get([path: "/users/2/messages/received", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
    }

    void "mod gets user2 message spread"() {
        when: "mod gets user2 message spread"
        Response response = clientModerator.get([path: "/users/2/messages/spread", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
    }

    void "mod gets reported messages"() {
        when: "mod gets reported messages"
        Response response = clientModerator.get([path: "/messages/reported", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
    }
}
