package it.uspread.core.service.ios

import grails.transaction.Transactional
import grails.util.Environment
import it.uspread.core.domain.User;

import org.codehaus.groovy.grails.io.support.ClassPathResource

import com.notnoop.apns.APNS
import com.notnoop.apns.ApnsService

/**
 * Service du système de notification PUSH Apple.
 */
@Transactional
class IosAPNSService {

    private ApnsService service

    IosAPNSService() {
        def certPath = null
        def certPass = null
        switch (Environment.current) {
            case Environment.DEVELOPMENT:
                certPath = new ClassPathResource("apns-dev.p12").getInputStream()
                certPass = "purzqqg.2.7"
                break
            case Environment.TEST:
                certPath = new ClassPathResource("apns-dev.p12").getInputStream()
                certPass = "purzqqg.2.7"
                break
            // TODO pour l'instant on pointe aussi sur l'environnement de dev
            case Environment.PRODUCTION:
                certPath = new ClassPathResource("apns-dev.p12").getInputStream()
                certPass = "purzqqg.2.7"
                break
        }

        service = APNS.newService().withCert(certPath, certPass).withSandboxDestination().build();
    }

    /**
     * Lance une notification push pour indiquer aux différents clients ios des utilisateurs concernés qu'un nouveau message est disponible.
     * @param listUser Les utilisateurs recevant un nouveau message
     */
    void notifyMessageSentTo(List<User> listUser) {
        for (User user : listUser) {
            Set<String> listIosPushtoken = user.iosPushTokens
            if (listIosPushtoken) {
                push(listIosPushtoken, "New message(s) spreading")
            }
        }
    }

    /**
     * Lance une notification push pour indiquer aux différents clients ios des utilisateurs concernés qu'un message a été supprimé
     * @param listUser les utilisateurs pouvant visualiser ce message (Exclus l'auteur du message)
     * @param messageId l'id du message qui est supprimé
     */
    void notifyMessageDeleteTo(Set<User> listUser, Long messageId) {
        for (User user : listUser) {
            Set<String> listIosPushtoken = user.iosPushTokens
            if (listIosPushtoken) {
                // TODO
            }
        }
    }

    /**
     * Réserve le push token donné exclusivement à l'utilisateur donné (Cas ou l'utilisateur change de compte sur son périphérique)
     * @param user un utilisateur
     * @param pushToken le token
     */
    void reservePushTokenToUser(User user, String pushToken) {
        // On le retire des autres user
        List<User> listUser = User.findAll("from User usr where usr.id <> :idUsr and :token in elements(usr.iosPushTokens)", [token: pushToken, idUsr: user.id])
        for (User usr : listUser) {
            usr.removeFromIosPushTokens(pushToken)
        }
        // On reaffecte le push token à l'user demandé
        registerPushToken(user, pushToken)
    }

    /** Enregistrement d'un token pour l'utilisateur (s'il est nouveau) */
    void registerPushToken(User user, String androidPushToken) {
        if (!user.iosPushTokens.contains(androidPushToken)) {
            user.addToIosPushTokens(androidPushToken)
        }
    }

    private void push(String token, String message) {
        String payload = APNS.newPayload().alertBody(message).build();
        service.push(token, payload);
    }
}
