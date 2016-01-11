package it.uspread.core

import grails.test.mixin.integration.Integration
import it.uspread.core.type.BackgroundType

import org.apache.commons.codec.binary.Base64
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
 * Ne peuvent être rejoués plusieurs fois (Pas de rollback pour ces test).
 */
@Integration
class MessageControllerFunctionalSpec extends Specification {

    private static final String BASE_URL = "http://localhost:8080/rest/"

    private static long idMessage1FromUser1
    private static long idMessage2FromUser1
    private static long idMessage3FromUser1_ToDelete
    private static long idMessage1FromUser2
    private static long idMessage2FromUser2_ToReport
    private static long idMessage1FromUser3
    private static long idMessage1FromUser4

    @Shared def clientPublic = new RESTClient(BASE_URL)
    @Shared def clientUser1 = new RESTClient(BASE_URL)
    @Shared def clientUser2 = new RESTClient(BASE_URL)
    @Shared def clientUser3 = new RESTClient(BASE_URL)
    @Shared def clientUser4 = new RESTClient(BASE_URL)
    @Shared def clientModerator = new RESTClient(BASE_URL)

    void setup() {
        clientUser1.authorization = new HTTPBasicAuthorization("user1", "user1")
        clientUser2.authorization = new HTTPBasicAuthorization("user2", "user2")
        clientUser3.authorization = new HTTPBasicAuthorization("user3", "user3")
        clientUser4.authorization = new HTTPBasicAuthorization("user4", "user4")
        clientModerator.authorization = new HTTPBasicAuthorization("mod", "mod")

        // TODO lorsqu'on aura mis en place SSL
        // clientPublic.httpClient.sslTrustAllCerts = true
        // clientUser1.httpClient.sslTrustAllCerts = true
        // clientUser2.httpClient.sslTrustAllCerts = true
        // clientUser3.httpClient.sslTrustAllCerts = true
        // clientUser4.httpClient.sslTrustAllCerts = true
    }

    private Map wrapPlainMessage(Map param) {
        param['backgroundType'] = BackgroundType.PLAIN.name()
        param['backgroundColor'] = 'FFBB33'
        param['textColor'] = '000000'
        return param
    }

    private Map wrapImageMessage(Map param) {
        param['backgroundType'] = BackgroundType.IMAGE.name()
        param['image'] = Base64.encodeBase64String([0, 0, 0, 0, 0, 1, 0] as byte[])
        param['textColor'] = '000000'
        return param
    }

    private long postMessage(RESTClient client, String text, boolean image = false) {
        Response response = client.post([path: "/messages", accept: ContentType.JSON]) {
            type(ContentType.JSON)
            json(image ? wrapImageMessage([text: text]) : wrapPlainMessage([text: text]))
        }

        assert response.statusCode == HttpStatus.CREATED.value
        assert response.json.id != null
        return response.json.id
    }

    void "user1 post pushtoken Android"() {
        // NECESSAIRE pour pouvoir tester un minimum la partie push Android
        when: "POST a pushtoken"
        Response response = clientUser1.post([path: "/users/connected/pushtoken"]) {
            type(ContentType.JSON)
            json([pushToken: "APA91bEuKPbl9edxSai9NfV1JMdnQTvhxKYCCrLvXDlg0GHTnEX20wj_QW9MupoZWoQW9MupoZWoQW9MupoZWoQW9MupoZWoQW9MupoZWooVhN8EahuQHzvC179OA5kJW4Oc7ybS2iWi9I2puR8EtDmebihL4bdiig", device: "ANDROID"])
        }

        then: "Its work"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "user2 post pushtoken OS"() {
        // NECESSAIRE pour pouvoir tester un minimum la partie push IOS
        when: "POST a pushtoken"
        Response response = clientUser1.post([path: "/users/connected/pushtoken"]) {
            type(ContentType.JSON)
            json([pushToken: "00000007bebcf74f9b7c25d48e3358945f67701da5ddb387462c7eaf61bbad78", device: "IOS"])
        }

        then: "Its work"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "Users post messages"() {
        when: "Users post messages"
        idMessage1FromUser1 = postMessage(clientUser1, "hello")
        idMessage2FromUser1 = postMessage(clientUser1, "hello world", true)
        idMessage3FromUser1_ToDelete = postMessage(clientUser1, "hello universe")
        idMessage1FromUser2 = postMessage(clientUser2, "he ho calmos")
        idMessage2FromUser2_ToReport = postMessage(clientUser2, "Fuck")
        idMessage1FromUser3 = postMessage(clientUser3, "Yo")
        idMessage1FromUser4 = postMessage(clientUser4, null, true) // Un message avec image et sans texte

        then: "no assert releved"
    }

    void "Unknow user try to access protected URL"() {
        when: "ee"
        then: "ee"
        // TODO faire pour toutes les URL protégé cf test User
    }

    void "Simple user try to access forbidden URL"() {

        // Les conditions spéciale (tentative de suppression d'un message d'un autre, ou de spread again the same etc sont dans le test avec le mot Troll)

        when: "ee"
        then: "ee"

        // TODO faire pour toutes les URL qu'il n'a pas accés cf test User
    }

    void "Moderator try to access forbidden URL"() {
        when: "ee"
        then: "ee"
        // TODO faire pour toutes les URL qu'il n'a pas accés cf test User
    }

    void "user 1 spread"() {
        when: "Message is spread by user1"
        Response response = clientUser1.post([path: "/messages/"+idMessage1FromUser2+"/spread", accept: ContentType.JSON]) {
            type(ContentType.JSON)
        }

        then: "Status code is"
        response.statusCode == HttpStatus.ACCEPTED.value
        response.json.size() == 3
        response.json.id != null
        response.json.nbSpread != null
        response.json.dateSpread != null
    }

    void "user 2 spread"() {
        when: "Message is spread by user2"
        Response response = clientUser2.post([path: "/messages/"+idMessage1FromUser1+"/spread", accept: ContentType.JSON]) {
            type(ContentType.JSON)
        }

        then: "Status code is"
        response.statusCode == HttpStatus.ACCEPTED.value
        response.json.size() == 3
        response.json.id != null
        response.json.nbSpread != null
        response.json.dateSpread != null
    }

    void "user 3 ignores"() {
        when: "Message is ignored by user3"
        Response response = clientUser3.post([path: "/messages/"+idMessage1FromUser1+"/ignore"]) {
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

    void "user 1 get a specific PLAIN message"() {
        when: "Message is got by user1"
        Response response = clientUser1.get([path: "/messages/"+idMessage1FromUser1, accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 7
        response.json.id != null
        response.json.dateCreated != null
        response.json.nbSpread != null
        response.json.text != null
        response.json.textColor != null
        response.json.backgroundType != null
        response.json.backgroundColor != null
    }

    void "user 1 get a specific IMAGE message"() {
        when: "Message is got by user1"
        Response response = clientUser1.get([path: "/messages/"+idMessage2FromUser1, accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 7
        response.json.id != null
        response.json.dateCreated != null
        response.json.nbSpread != null
        response.json.text != null
        response.json.textColor != null
        response.json.backgroundType != null
        response.json.image != null
    }

    void "user 1 get a specific IMAGE message but only the image"() {
        when: "Message is got by user1"
        Response response = clientUser1.get([path: "/messages/"+idMessage2FromUser1+"?onlyImage=true", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 2
        response.json.id != null
        response.json.image != null
    }

    void "user 1 delete one of his message"() {
        when: "Message is deleted"
        Response response = clientUser1.delete([path: "/messages/"+idMessage3FromUser1_ToDelete])

        then: "Status code is"
        response.statusCode == HttpStatus.ACCEPTED.value
    }

    void "user 1 gets his messages"() {
        when: "Messages are got by user1"
        Response response = clientUser1.get([path: "/messages?query=AUTHOR", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 2
    }

    void "user 1 gets his messages received"() {
        when: "Messages are got by user1"
        Response response = clientUser1.get([path: "/messages?query=RECEIVED", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 2
    }

    void "user 1 gets his messages spread"() {
        when: "Messages are got by user1"
        Response response = clientUser1.get([path: "/messages?query=SPREAD", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 1
    }

    void "mod gets user1 messages"() {
        when: "mod gets user1 messages"
        Response response = clientModerator.get([path: "/users/1/messages", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 2
    }

    void "mod gets user2 message received"() {
        when: "mod gets user2 message received"
        Response response = clientModerator.get([path: "/users/2/messages/received", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 2
    }

    void "mod gets user2 message spread"() {
        when: "mod gets user2 message spread"
        Response response = clientModerator.get([path: "/users/2/messages/spread", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 1
    }

    void "mod gets reported messages"() {
        when: "mod gets reported messages"
        Response response = clientModerator.get([path: "/messages/reported", accept: ContentType.JSON])

        then: "Status code is"
        response.statusCode == HttpStatus.OK.value
        response.json.size() == 1
    }

    void "Simple user try to Troll the server"() {
        try {
            when: "A user try to delete a message of another user"
            Response response = clientUser1.delete([path: "/messages/"+idMessage1FromUser2])
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "A user try to spread a message he has writed"
            Response response = clientUser1.post([path: "/messages/"+idMessage1FromUser1+"/spread", accept: ContentType.JSON]) {
                type(ContentType.JSON)
            }
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "A user try to spread a message he has already spread"
            Response response = clientUser2.post([path: "/messages/"+idMessage1FromUser2+"/spread", accept: ContentType.JSON]) {
                type(ContentType.JSON)
            }
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "A user try to ignore a message he has writed"
            Response response = clientUser1.post([path: "/messages/"+idMessage1FromUser1+"/ignore", accept: ContentType.JSON]) {
                type(ContentType.JSON)
            }
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "A user try to ignore a message he has already ignored"
            Response response = clientUser3.post([path: "/messages/"+idMessage1FromUser1+"/ignore"]) {
                type(ContentType.JSON)
            }
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "A user try to report a message he has writed"
            Response response = clientUser1.post([path: "/messages/"+idMessage1FromUser1+"/report?type=SPAM"]) {
                type(ContentType.JSON)
            }
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }

        try {
            when: "A user try to report a message he has already reported"
            Response response = clientUser4.post([path: "/messages/"+idMessage2FromUser2_ToReport+"/report?type=SPAM"]) {
                type(ContentType.JSON)
            }
            false
        } catch(HTTPClientException e) {
            then: "is forbidden"
            e.response.statusCode == HttpStatus.FORBIDDEN.value
        }
    }

}
