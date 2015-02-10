package it.uspread.core

import spock.lang.FailsWith
import spock.lang.Shared
import spock.lang.Specification
import wslite.http.HTTPClientException
import wslite.http.auth.HTTPBasicAuthorization
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.Response

/**
 * Tests qui servent à valider les refactoring important
 * Le serveur doit tourner (en localhost) avant exécution
 * Ne peuvent être rejoués plusieurs fois
 */
class UserControllerFunctionalSpec extends Specification {

    @Shared def restClient = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def restClientToDelete = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def restModClient = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def restSignupClient = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def restModoClient = new RESTClient("http://localhost:8080/uSpread-core/rest/")

    void setup() {
        restClient.authorization = new HTTPBasicAuthorization("user1", "user1")
        restModClient.authorization = new HTTPBasicAuthorization("mod", "mod")
        restClientToDelete.authorization = new HTTPBasicAuthorization("user5", "user5")
        restModoClient.authorization = new HTTPBasicAuthorization("modo", "modo")
        // TODO lorsqu'on aura mis en place SSL
        // restUser1Client.httpClient.sslTrustAllCerts = true
        // restModClient.httpClient.sslTrustAllCerts = true
        // restClientToDelete.httpClient.sslTrustAllCerts = true
        // restSignupClient.httpClient.sslTrustAllCerts = true
        // restModoClient.httpClient.sslTrustAllCerts = true
    }

    void "GET user with id 2"() {
        when: "GET user with id 2"
        Response response = restModClient.get(path: "/users/2", accept: ContentType.JSON)

        then: "I get the expected user as a JSON"
        response.statusCode == 200
        response.json.username == "user1"
    }

    void "Put user with id 2"() {
        when: "Put user with id 2"
        Response response = restModClient.put(path: "/users/2") {
            type ContentType.JSON
            json email:"jp@toto.fr"
        }
        response = restModClient.get(path: "/users/2", accept: ContentType.JSON)

        then: "I get the expected user as a JSON"
        response.statusCode == 200
        response.json.email == "jp@toto.fr"
    }

    @FailsWith(HTTPClientException)
    void "delete user with id 4"() {
        when: "delete user with id 4"
        Response response = restModClient.delete(path: "/users/4")

        then: "I try to get the expected user as a JSON"
        restModClient.get(path: "/users/4", accept: ContentType.JSON)
    }

    void "GET user connected"() {
        when: "GET user connected"
        Response response = restClient.get(path: "/users/connected", accept: ContentType.JSON)

        then: "I get the expected user as a JSON"
        response.statusCode == 200
        response.json.username == "user1"
    }

    void "Put user connected"() {
        when: "Put user connected"
        Response response = restClient.put(path: "/users/connected") {
            type ContentType.JSON
            json email:"jpa@toto.fr"
        }
        response = restClient.get(path: "/users/connected", accept: ContentType.JSON)

        then: "I get the expected user as a JSON"
        response.statusCode == 200
        response.json.email == "jpa@toto.fr"
    }

    @FailsWith(HTTPClientException)
    void "delete user connected"() {
        when: "delete user connected"
        Response response = restClientToDelete.delete(path: "/users/connected")

        then: "I try to get the expected user as a JSON"
        restClientToDelete.get(path: "/users/connected", accept: ContentType.JSON)
    }

    void "Signup"() {
        when: "Trying to signup"
        Response response = restSignupClient.post(path: "/signup") {
            type ContentType.JSON
            json email:"chuck@norris.fr", username:"chuck", password:"chuck"
        }
        restSignupClient.authorization = new HTTPBasicAuthorization("chuck", "chuck")
        response = restSignupClient.get(path: "/users/connected", accept: ContentType.JSON)

        then: "I get the expected user as a JSON"
        response.statusCode == 200
        response.json.email == "chuck@norris.fr"
    }

    void "Create Moderator"() {
        when: "Trying to create a mod"
        Response response = restModClient.post(path: "/users/moderator") {
            type ContentType.JSON
            json email:"modo@free.fr", username:"modo", password:"modo"
        }
        response = restModoClient.get(path: "/users/connected", accept: ContentType.JSON)

        then: "I get the expected user as a JSON"
        response.statusCode == 200
        response.json.email == "modo@free.fr"
    }

    void "GET user list"() {
        when: "GET user list"
        Response response = restModClient.get(path: "/userlist", accept: ContentType.JSON)

        then: "I get the list as a JSON"
        response.statusCode == 200
        response.json[0].username != null
    }

    void "GET topusers"() {
        when: "GET topusers"
        Response response = restClient.get(path: "/topusers", accept: ContentType.JSON)

        then: "I get the list as a JSON"
        response.statusCode == 200
        response.json[0].username != null
    }
}
