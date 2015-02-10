package it.uspread.core

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

    long idMessageFromUser1 = -1
    long idMessageFromUser1ToDelete = -1

    @Shared def restModClient = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def restUser1Client = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def restUser2Client = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def restUser3Client = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def restUser4Client = new RESTClient("http://localhost:8080/uSpread-core/rest/")

    void setup() {
        restModClient.authorization = new HTTPBasicAuthorization("mod", "mod")
        restUser1Client.authorization = new HTTPBasicAuthorization("user1", "user1")
        restUser2Client.authorization = new HTTPBasicAuthorization("user2", "user2")
        restUser3Client.authorization = new HTTPBasicAuthorization("user3", "user3")
        restUser4Client.authorization = new HTTPBasicAuthorization("user4", "user4")

        // TODO lorsqu'on aura mis en place SSL
        // restUserModClient.httpClient.sslTrustAllCerts = true
        // restUser1Client.httpClient.sslTrustAllCerts = true
        // restUser2Client.httpClient.sslTrustAllCerts = true
        // restUser3Client.httpClient.sslTrustAllCerts = true
        // restUser4Client.httpClient.sslTrustAllCerts = true

        // user 1 posts a message
        restUser1Client.post(path: "/messages") {
            type ContentType.JSON
            json text:"hello world"
        }
        restUser1Client.post(path: "/messages") {
            type ContentType.JSON
            json text:"hello world"
        }
        idMessageFromUser1 = restUser2Client.get(path: "/messages?query=RECEIVED", accept: ContentType.JSON).json[-1].id
        idMessageFromUser1ToDelete = idMessageFromUser1.longValue() + 1
    }

    void "user 2 spread"() {
        when: "Message is spread by user2"
        Response response = restUser2Client.post(path: "/messages/"+idMessageFromUser1+"/spread"){
            type ContentType.JSON
        }

        then: "Status code is"
        response.statusCode == 204
    }

    void "user 3 ignores"() {
        when: "Message is ignored by user3"
        Response response = restUser3Client.post(path: "/messages/"+idMessageFromUser1+"/ignore"){
            type ContentType.JSON
        }

        then: "Status code is"
        response.statusCode == 204
    }

    void "user 4 reports"() {
        when: "Message is reported by user4"
        Response response = restUser4Client.post(path: "/messages/"+idMessageFromUser1+"/report?type=SPAM"){
            type ContentType.JSON
        }

        then: "Status code is"
        response.statusCode == 204
    }

    void "user 1 gets his messages"() {
        when: "Messages are got by user1"
        Response response = restUser1Client.get(path: "/messages?query=AUTHOR")

        then: "Status code is"
        response.statusCode == 200
        response.json[0] != null
    }

    void "user 1 gets his message"() {
        when: "Message is got by user1"
        Response response = restUser1Client.get(path: "/messages/"+idMessageFromUser1)

        then: "Status code is"
        response.statusCode == 200
        response.json.id != null
    }

    void "user 1 deletes a message"() {
        when: "Message is deleted"
        Response response = restUser1Client.delete(path: "/messages/"+idMessageFromUser1ToDelete)

        then: "Status code is"
        response.statusCode == 204
    }

    void "user 1 gets his status"() {
        when: "status is got by user1"
        Response response = restUser1Client.get(path: "/users/connected/status")

        then: "Status code is"
        response.statusCode == 200
    }

    void "mod gets user1 messages"() {
        when: "mod gets user1 messages"
        Response response = restModClient.get(path: "/users/2/messages")

        then: "Status code is"
        response.statusCode == 200
    }

    void "mod gets user2 message received"() {
        when: "mod gets user2 message received"
        Response response = restModClient.get(path: "/users/3/messages/received")

        then: "Status code is"
        response.statusCode == 200
    }

    void "mod gets user3 message spread"() {
        when: "mod gets user3 message spread"
        Response response = restModClient.get(path: "/users/4/messages/spread")

        then: "Status code is"
        response.statusCode == 200
    }

    void "mod gets reported messages"() {
        when: "mod gets reported messages"
        Response response = restModClient.get(path: "/messages/reported")

        then: "Status code is"
        response.statusCode == 200
    }
}
