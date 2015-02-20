package it.uspread.android

import grails.transaction.Transactional
import it.uspread.core.Message
import it.uspread.core.User
import it.uspread.core.json.JSONAttribute

import com.google.android.gcm.server.Constants
import com.google.android.gcm.server.MulticastResult
import com.google.android.gcm.server.Result

/**
 * Service du système de notification PUSH Android.
 * FIXME optimisation des requetes car là c'est pas ok sauf si Grails est super bon pour comprendre
 */
@Transactional
class AndroidGcmPushService {

    /** Spring inject : androidGcmService */
    def androidGcmService
    /** Spring inject : grailsApplication */
    def grailsApplication

    /**
     * Lance une notification push pour indiquer aux différents clients android des utilisateurs concernés qu'un nouveau message est disponible.
     * @param listUser Les utilisateurs recevant un nouveau message
     */
    public void notifyMessageSentTo(List<User> listUser) {
        for (User user : listUser) {
            List<String> listAndroidPushtoken = user.androidPushTokens.collect()
            if (listAndroidPushtoken != null && !listAndroidPushtoken.isEmpty()) {
                def data = [type: "SYNC", (JSONAttribute.USER_USERNAME): user.username]
                def result = androidGcmService.sendMessage(data, listAndroidPushtoken, "New Message ${JSONAttribute.USER_USERNAME}")
                if (listAndroidPushtoken.size() > 1) {
                    analyseMultiCastResult(result, listAndroidPushtoken)
                } else {
                    analyseResult(result, listAndroidPushtoken.get(0))
                }
            }
        }
    }

    /**
     * Lance une notification push pour indiquer aux différents clients android des utilisateurs concernés qu'un message a été supprimé
     * @param listUser les utilisateurs pouvant visualiser ce message (Exclus l'auteur du message)
     * @param message le message qui est supprimé
     */
    public void notifyMessageDeleteTo(List<User> listUser, Message message) {
        for (User user : listUser) {
            List<String> listAndroidPushtoken = user.androidPushTokens.collect()
            if (listAndroidPushtoken != null && !listAndroidPushtoken.isEmpty()) {
                def data = [type: "DELETE", (JSONAttribute.USER_USERNAME): user.username, (JSONAttribute.MESSAGE_ID): message.id]
                def result = androidGcmService.sendMessage(data, listAndroidPushtoken)
                if (listAndroidPushtoken.size() > 1) {
                    analyseMultiCastResult(result, listAndroidPushtoken)
                } else {
                    analyseResult(result, listAndroidPushtoken.get(0))
                }
            }
        }
    }

    /**
     * Réserve le push token donné exclusivement à l'utilisateur donné
     * @param user un utilisateur
     * @param pushToken le token
     */
    public void reservePushTokenToUser(User user, String pushToken) {
        // On le retire des autres user
        List<User> listUser = User.findAll("from User usr where usr.id <> :idUsr and :token in elements(usr.androidPushTokens)", [token: pushToken, idUsr: user.id])
        for (User usr : listUser) {
            usr.androidPushTokens.remove(pushToken)
        }
        // On reaffecte le push token à l'user demandé
        registerPushToken(user, pushToken)
    }

    /** Enregistrement d'un nouveau token pour l'utilisateur */
    public void registerPushToken(User user, String androidPushToken) {
        if (!user.androidPushTokens.contains(androidPushToken)) {
            user.androidPushTokens.add(androidPushToken)
        }
    }

    /**
     * Analyse le retour multicast du PUSH
     * @param results Resultat du PUSH
     * @param listAndroidPushtoken
     */
    private void analyseMultiCastResult(MulticastResult results, List<String> listAndroidPushtoken) {
        for (int i = 0; i < listAndroidPushtoken.size(); i++) {
            String currentPushToken = listAndroidPushtoken.get(i)
            Result result = results.getResults().get(i)
            analyseResult(result, currentPushToken)
        }
    }

    /**
     * Analyse le retour simple du PUSH
     * @param result Resultat du PUSH
     * @param androidPushToken
     */
    private void analyseResult(Result result, String androidPushToken) {
        // Si le message a été bien transmis
        if (result.getMessageId() != null) {
            String canonicalRegId = result.getCanonicalRegistrationId()
            // Si un token plus récent est fourni alors le remplacer
            if (canonicalRegId != null) {
                List<User> listUser = User.findAll("from User usr where :token in elements(usr.androidPushTokens)", [token: androidPushToken])
                for (User user : listUser) {
                    user.androidPushTokens.remove(androidPushToken)
                    user.androidPushTokens.add(canonicalRegId);
                }

            }
        }
        // Si le message n'a pas été transmis
        else {
            String error = result.getErrorCodeName();
            // L'application c'est désenregister : retirer le token
            if (Constants.ERROR_NOT_REGISTERED.equals(error)) {
                List<User> listUser = User.findAll("from User usr where :token in elements(usr.androidPushTokens)", [token: androidPushToken])
                for (User user : listUser) {
                    user.androidPushTokens.remove(androidPushToken)
                }
            }
        }
    }
}
