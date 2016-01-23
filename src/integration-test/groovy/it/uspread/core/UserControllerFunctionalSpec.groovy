package it.uspread.core

import grails.test.mixin.integration.Integration
import it.uspread.core.json.JSONAttribute
import it.uspread.core.params.URLParamsName

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
 * Ne peuvent être rejoués plusieurs fois (Pas de rollback pour ces test)
 */
@Integration
class UserControllerFunctionalSpec extends Specification {

    private static final String BASE_URL = "http://localhost:8080/rest/"

    @Shared def clientPublic = new RESTClient(BASE_URL)
    @Shared def clientUser1 = new RESTClient(BASE_URL)
    @Shared def clientUser2_NoChange = new RESTClient(BASE_URL)
    @Shared def clientUser6_ToDelete = new RESTClient(BASE_URL)
    @Shared def clientModerator = new RESTClient(BASE_URL)
    @Shared def clientModerator_ToDelete = new RESTClient(BASE_URL)

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
                json(["$JSONAttribute.USER_PUSHTOKEN": "AZERTYUIOPQSDFGHJKLM", "$JSONAttribute.USER_DEVICE": "ANDROID"])
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
            Response response = clientPublic.get([path: "/users", accept: ContentType.JSON])
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
            Response response = clientUser1.get([path: "/users", accept: ContentType.JSON])
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
                json(["$JSONAttribute.USER_PUSHTOKEN": "AZERTYUIOPQSDFGHJKLM", "$JSONAttribute.USER_DEVICE": "ANDROID"])
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
            json(["$JSONAttribute.USER_EMAIL":"chuck@norris.fr", "$JSONAttribute.USER_USERNAME":"chuck", "$JSONAttribute.USER_PASSWORD":"chuck"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.CREATED.value

        when: "Trying to get this new user"
        def newclient = new RESTClient(BASE_URL)
        newclient.authorization = new HTTPBasicAuthorization("chuck", "chuck")
        response = newclient.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[JSONAttribute.USER_EMAIL] == "chuck@norris.fr"
    }

    void "Login Moderator"() {
        when: "Trying to login"
        Response response = clientModerator.post([path: "/login"]) {
            type(ContentType.JSON)
            json(["$JSONAttribute.USER_USERNAME":"mod", "$JSONAttribute.USER_PASSWORD":"mod"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "Login Simple user"() {
        when: "Trying to login"
        Response response = clientUser1.post([path: "/login?" + URLParamsName.USER_PUSHTOKEN + "=AZERTYUIOPQSDFGHJKLM&" + URLParamsName.USER_DEVISE + "=ANDROID"]) {
            type(ContentType.JSON)
            json(["$JSONAttribute.USER_USERNAME":"user1", "$JSONAttribute.USER_PASSWORD":"user1"])
        }
        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "Moderator Create Moderator"() {
        when: "Trying to create a moderator"
        Response response = clientModerator.post([path: "/users/moderator"]) {
            type(ContentType.JSON)
            json(["$JSONAttribute.USER_EMAIL":"modo@free.fr", "$JSONAttribute.USER_USERNAME":"modo", "$JSONAttribute.USER_PASSWORD":"modo"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.CREATED.value

        when: "Trying to get this new moderator"
        def newModo = new RESTClient(BASE_URL)
        newModo.authorization = new HTTPBasicAuthorization("modo", "modo")
        response = newModo.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected moderator as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[JSONAttribute.USER_EMAIL] == "modo@free.fr"
    }

    void "Moderator GET user with id 2"() {
        when: "GET user with id 2"
        Response response = clientModerator.get([path: "/users/2", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 7
        response.json[JSONAttribute.USER_ID] == 2
        response.json[JSONAttribute.USER_USERNAME] == "user2"
        response.json[JSONAttribute.USER_EMAIL] == "user2@42.fr"
        response.json[JSONAttribute.USER_ROLE] == "PUBLIC"
        response.json[JSONAttribute.USER_REPORTSSENT] == 0
        response.json[JSONAttribute.USER_REPORTSRECEIVED] == 0
        response.json[JSONAttribute.USER_MODERATIONREQUIRED] == false
    }

    void "Moderator Put user with id 1"() {
        when: "Put user with id 1"
        Response response = clientModerator.put([path: "/users/1"]) {
            type(ContentType.JSON)
            json(["$JSONAttribute.USER_USERNAME":"user1", "$JSONAttribute.USER_EMAIL":"jp@toto.fr"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value

        when: "Trying to get the new values"
        response = clientModerator.get([path: "/users/1", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[JSONAttribute.USER_EMAIL] == "jp@toto.fr"
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
        Response response = clientModerator.get([path: "/users", accept: ContentType.JSON])

        then: "I get the list as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[0][JSONAttribute.USER_USERNAME] != null

        // TODO ajouter le test des différents critères disponibles
    }

    void "Moderator GET user connected"() {
        when: "GET user connected"
        Response response = clientModerator.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 4
        response.json[JSONAttribute.USER_ID] == 8
        response.json[JSONAttribute.USER_USERNAME] == "mod"
        response.json[JSONAttribute.USER_EMAIL] == "mod@uspread.it"
        response.json[JSONAttribute.USER_ROLE] == "MODERATOR"
    }

    void "Moderator Put user connected"() {
        when: "Put user connected"
        Response response = clientModerator.put([path: "/users/connected"]) {
            type(ContentType.JSON)
            json(["$JSONAttribute.USER_USERNAME": "mod" ,"$JSONAttribute.USER_EMAIL": "jpamodo@toto.fr"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value

        when: "Trying to get the new values"
        response = clientModerator.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[JSONAttribute.USER_EMAIL] == "jpamodo@toto.fr"
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
        Response response = clientModerator.get([path: "/users/topusers", accept: ContentType.JSON])

        then: "I get the list as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[0][JSONAttribute.USER_USERNAME] != null

        // TODO paufiner ce test en même temps que la fonctionnalité
    }

    void "Simple user GET user connected"() {
        when: "GET user connected"
        Response response = clientUser2_NoChange.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 3
        response.json[JSONAttribute.USER_ID] == 2
        response.json[JSONAttribute.USER_USERNAME] == "user2"
        response.json[JSONAttribute.USER_EMAIL] == "user2@42.fr"
    }

    void "Simple user Put user connected"() {
        when: "Put user connected"
        Response response = clientUser1.put([path: "/users/connected"]) {
            type(ContentType.JSON)
            json(["$JSONAttribute.USER_USERNAME": "user1", "$JSONAttribute.USER_EMAIL":"jpa@toto.fr"])
        }

        then: "Look like ok"
        response.statusCode == HttpStatus.ACCEPTED.value

        when: "Trying to get the new values"
        response = clientUser1.get([path: "/users/connected", accept: ContentType.JSON])

        then: "I get the expected user as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[JSONAttribute.USER_EMAIL] == "jpa@toto.fr"
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
        Response response = clientUser1.get([path: "/users/connected/status?" + URLParamsName.USER_ONLY_QUOTA + "=true", accept: ContentType.JSON])

        then: "Its work"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 2
        response.json[JSONAttribute.STATUS_WORLDQUOTAREACHED] != null
        response.json[JSONAttribute.STATUS_LOCALQUOTAREACHED] != null

        when: "Get his status"
        response = clientUser1.get([path: "/users/connected/status", accept: ContentType.JSON])

        then: "Its work"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 6
        response.json[JSONAttribute.STATUS_WORLDQUOTAREACHED] != null
        response.json[JSONAttribute.STATUS_LOCALQUOTAREACHED] != null
        response.json[JSONAttribute.STATUS_NBMESSAGEWRITED] != null
        response.json[JSONAttribute.STATUS_NBMESSAGESPREAD] != null
        response.json[JSONAttribute.STATUS_NBMESSAGEIGNORED] != null
        response.json[JSONAttribute.STATUS_NBMESSAGEREPORTED] != null
    }

    void "Simple user post pushtoken"() {
        when: "POST a pushtoken"
        Response response = clientUser1.post([path: "/users/connected/pushtoken"]) {
            type(ContentType.JSON)
            json(["$JSONAttribute.USER_PUSHTOKEN": "AZERTYUIOPQSDFGHJKLM", "$JSONAttribute.USER_DEVICE": "ANDROID"])
        }

        then: "Its work"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "Simple user GET topusers"() {
        when: "GET topusers"
        Response response = clientUser1.get([path: "/users/topusers", accept: ContentType.JSON])

        then: "I get the list as a JSON"
        response.statusCode == HttpStatus.OK.value
        response.json[0][JSONAttribute.USER_USERNAME] != null

        // TODO paufiner ce test en meme temps que la fonctionnalité
    }
}
