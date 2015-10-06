package it.uspread.core

import grails.test.mixin.integration.Integration

import org.springframework.http.HttpStatus

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
 * Ne peuvent être rejoués plusieurs fois (Pas de rollback pour ces test)
 */
@Integration
class UserControllerFunctionalSpec extends Specification {

    @Shared def clientPublic = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def clientUser1 = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def clientUser2_NoChange = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def clientUser6_ToDelete = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def clientModerator = new RESTClient("http://localhost:8080/uSpread-core/rest/")
    @Shared def clientModerator_ToDelete = new RESTClient("http://localhost:8080/uSpread-core/rest/")

    void setup() {
        clientUser1.authorization = new HTTPBasicAuthorization("user1", "user1")
        clientUser2_NoChange.authorization = new HTTPBasicAuthorization("user2", "user2")
        clientUser6_ToDelete.authorization = new HTTPBasicAuthorization("user6", "user6")
        clientModerator.authorization = new HTTPBasicAuthorization("mod", "mod")
        clientModerator_ToDelete.authorization = new HTTPBasicAuthorization("old_mod", "old_mod")
        // TODO lorsqu'on aura mis en place SSL
        // clientPublic.httpClient.sslTrustAllCerts = true
        // clientUser1.httpClient.sslTrustAllCerts = true
        // clientUser2_NoChange.httpClient.sslTrustAllCerts = true
        // clientUser6_ToDelete.httpClient.sslTrustAllCerts = true
        // clientModerator.httpClient.sslTrustAllCerts = true
        // clientModerator_ToDelete.httpClient.sslTrustAllCerts = true
    }

    void "Unknow user try to access protected URL"() {

        try {
            when: "GET user connected"
            Response response = clientPublic.get([path: "/users/connected", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "PUT user connected"
            Response response = clientPublic.get([path: "/users/connected", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "DELETE user connected"
            Response response = clientPublic.get([path: "/users/connected", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "Get his status"
            Response response = clientPublic.get([path: "/users/connected/status", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "POST a pushtoken"
            Response response = clientPublic.post([path: "/users/connected/pushtoken"]) {
                type(ContentType.JSON)
                json([pushToken: "AZERTYUIOPQSDFGHJKLM", device: "ANDROID"])
            }
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "GET an user"
            Response response = clientPublic.get([path: "/users/1", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is unauthorized"
            e.response.statusCode == HttpStatus.UNAUTHORIZED.value
        }

        try {
            when: "DELETE an user"
            Response response = clientPublic.delete([path: "/users/1"])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "PUT an user"
            Response response = clientPublic.put([path: "/users/1"])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "POST an moderator"
            Response response = clientPublic.post([path: "/users/moderator"])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "get the user list"
            Response response = clientPublic.get([path: "/userlist", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }
    }

    void "Simple user try to access forbidden URL"() {
        try {
            when: "GET an user"
            Response response = clientUser1.get([path: "/users/1", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "DELETE an user"
            Response response = clientUser1.delete([path: "/users/1"])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "PUT an user"
            Response response = clientUser1.put([path: "/users/1"])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "POST an moderator"
            Response response = clientUser1.post([path: "/users/moderator"])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "get the user list"
            Response response = clientUser1.get([path: "/userlist", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }
    }

    void "Moderator try to access forbidden URL"() {
        try {
            when: "Get the status"
            Response response = clientModerator.get([path: "/users/connected/status", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "POST a pushtoken"
            Response response = clientModerator.post([path: "/users/connected/pushtoken"]) {
                type(ContentType.JSON)
                json([pushToken: "AZERTYUIOPQSDFGHJKLM", device: "ANDROID"])
            }
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }
    }

    void "Signup"() {
        when: "Trying to signup"
        Response response = clientPublic.post([path: "/signup"]) {
            type(ContentType.JSON)
            json([email:"chuck@norris.fr", username:"chuck", password:"chuck"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.CREATED.value

        when: "Trying to get this new user"
        def newclient = new RESTClient("http://localhost:8080/uSpread-core/rest/")
        newclient.authorization = new HTTPBasicAuthorization("chuck", "chuck")
        response = newclient.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.email == "chuck@norris.fr"
    }

    void "Login Moderator"() {
        when: "Trying to login"
        Response response = clientModerator.post([path: "/login"]) {
            type(ContentType.JSON)
            json([username:"mod", password:"mod"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "Login Simple user"() {
        when: "Trying to login"
        Response response = clientUser1.post([path: "/login?pushToken=AZERTYUIOPQSDFGHJKLM&device=ANDROID"]) {
            type(ContentType.JSON)
            json([username:"user1", password:"user1"])
        }
        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "Moderator Create Moderator"() {
        when: "Trying to create a moderator"
        Response response = clientModerator.post([path: "/users/moderator"]) {
            type(ContentType.JSON)
            json([email:"modo@free.fr", username:"modo", password:"modo"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.CREATED.value

        when: "Trying to get this new moderator"
        def newModo = new RESTClient("http://localhost:8080/uSpread-core/rest/")
        newModo.authorization = new HTTPBasicAuthorization("modo", "modo")
        response = newModo.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected moderator as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.email == "modo@free.fr"
    }

    void "Moderator GET user with id 2"() {
        when: "GET user with id 2"
        Response response = clientModerator.get([path: "/users/2", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 7
        response.json.id == 2
        response.json.username == "user2"
        response.json.email == "user2@42.fr"
        response.json.role == "USER"
        response.json.reportsSent == 0
        response.json.reportsReceived == 1
        response.json.moderationRequired == true
    }

    void "Moderator Put user with id 1"() {
        when: "Put user with id 1"
        Response response = clientModerator.put([path: "/users/1"]) {
            type(ContentType.JSON)
            json([username:"user1", email:"jp@toto.fr"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value

        when: "Trying to get the new values"
        response = clientModerator.get([path: "/users/1", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.email == "jp@toto.fr"
    }

    void "Moderator Delete user with id 7"() {
        when: "delete user with id 7"
        Response response = clientModerator.delete([path: "/users/7"])

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value

        try {
            when: "I try to get the expected user as a JSON"
            clientModerator.get([path: "/users/7", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is not found"
            e.response.statusCode == HttpStatus.NOT_FOUND.value
        }
    }

    void "Moderator GET user list"() {
        when: "GET user list"
        Response response = clientModerator.get([path: "/userlist", accept: ContentType.JSON])

        then: "I get the list as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[0].username != null

        // TODO ajouter le test des différents critères disponibles
    }

    void "Moderator GET user connected"() {
        when: "GET user connected"
        Response response = clientModerator.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 4
        response.json.id == 8
        response.json.username == "mod"
        response.json.email == "mod@42.fr"
        response.json.role == "MODERATOR"
    }

    void "Moderator Put user connected"() {
        when: "Put user connected"
        Response response = clientModerator.put([path: "/users/connected"]) {
            type(ContentType.JSON)
            json([username: "mod" ,email: "jpamodo@toto.fr"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value

        when: "Trying to get the new values"
        response = clientModerator.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.email == "jpamodo@toto.fr"
    }

    void "Moderator Delete user connected"() {
        when: "delete user connected"
        Response response = clientModerator_ToDelete.delete([path: "/users/connected"])

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value

        try {
            when: "I try to get the expected user as a JSON"
            clientModerator_ToDelete.get([path: "/users/connected", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is not found"
            e.response.statusCode == HttpStatus.UNAUTHORIZED.value
        }
    }

    void "Moderator GET topusers"() {
        when: "GET topusers"
        Response response = clientModerator.get([path: "/topusers", accept: ContentType.JSON])

        then: "I get the list as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[0].username != null

        // TODO paufiner ce test en même temps que la fonctionnalité
    }

    void "Simple user GET user connected"() {
        when: "GET user connected"
        Response response = clientUser2_NoChange.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 3
        response.json.id == 2
        response.json.username == "user2"
        response.json.email == "user2@42.fr"
    }

    void "Simple user Put user connected"() {
        when: "Put user connected"
        Response response = clientUser1.put([path: "/users/connected"]) {
            type(ContentType.JSON)
            json([username: "user1", email:"jpa@toto.fr"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value

        when: "Trying to get the new values"
        response = clientUser1.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.email == "jpa@toto.fr"
    }

    void "Simple user delete user connected"() {
        when: "delete user connected"
        Response response = clientUser6_ToDelete.delete([path: "/users/connected"])

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value

        try {
            when: "I try to get the expected user as a JSON"
            clientUser6_ToDelete.get([path: "/users/connected", accept: ContentType.JSON])
            false
        } catch(HTTPClientException e) {
            then: "is not found"
            e.response.statusCode == HttpStatus.UNAUTHORIZED.value
        }
    }

    void "Simple user get status"() {
        when: "Get his status"
        Response response = clientUser1.get([path: "/users/connected/status?quotaOnly=true", accept: ContentType.JSON])

        then: "Its work"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 1
        response.json.quotaReached != null

        when: "Get his status"
        response = clientUser1.get([path: "/users/connected/status", accept: ContentType.JSON])

        then: "Its work"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 5
        response.json.quotaReached != null
        response.json.nbMessageWrited != null
        response.json.nbMessageSpread != null
        response.json.nbMessageIgnored != null
        response.json.nbMessageReported != null
    }

    void "Simple user post pushtoken"() {
        when: "POST a pushtoken"
        Response response = clientUser1.post([path: "/users/connected/pushtoken"]) {
            type(ContentType.JSON)
            json([pushToken: "AZERTYUIOPQSDFGHJKLM", device: "ANDROID"])
        }

        then: "Its work"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "Simple user GET topusers"() {
        when: "GET topusers"
        Response response = clientUser1.get([path: "/topusers", accept: ContentType.JSON])

        then: "I get the list as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[0].username != null

        // TODO paufiner ce test en meme temps que la fonctionnalité
    }
}
