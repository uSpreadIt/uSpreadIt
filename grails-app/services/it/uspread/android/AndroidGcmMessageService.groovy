package it.uspread.android

import grails.transaction.Transactional
import it.uspread.core.Message
import it.uspread.core.User
import it.uspread.core.json.JSONAttribute

import com.google.android.gcm.server.Constants
import com.google.android.gcm.server.MulticastResult
import com.google.android.gcm.server.Result

@Transactional
class AndroidGcmMessageService {

    def androidGcmService
    def grailsApplication
    def userService

    /**
     * Lance une notification push pour indiquer aux différents clients android des utilisateurs concernés qu'un nouveau message est disponible.
     * @param listUser Les utilisateurs recevant un nouveau message
     */
    def notifyMessageSentTo(List<User> listUser) {
        for (User user : listUser) {
            List<String> listAndroidPushtoken = user.androidPushTokens.collect()
            if (listAndroidPushtoken != null && !listAndroidPushtoken.isEmpty()) {
                def data = ["${JSONAttribute.USER_ID}": user.id]
                MulticastResult results = androidGcmService.sendMulticastCollapseMessage("New Message", data, listAndroidPushtoken)
                analyseResult(results, listAndroidPushtoken)
            }
        }
    }

    /**
     * Lance une notification push pour indiquer aux différents clients android des utilisateurs concernés qu'un message a été supprimé
     * @param listUser les utilisateurs pouvant visualiser ce message (Exclus l'auteur du message)
     * @param message le message qui est supprimé
     */
    def notifyMessageDeleteTo(List<User> listUser, Message message) {
        for (User user : listUser) {
            List<String> listAndroidPushtoken = user.androidPushTokens.collect()
            if (listAndroidPushtoken != null && !listAndroidPushtoken.isEmpty()) {
                def data = ["${JSONAttribute.USER_ID}": user.id, "${JSONAttribute.MESSAGE_ID}": message.id]
                MulticastResult results = androidGcmService.sendMulticastInstantMessage(data, listAndroidPushtoken)
                analyseResult(results, listAndroidPushtoken)
            }
        }
    }

    /**
     * Analyse le retour du PUSH
     * @param result Resultat du PUSH
     * @return
     */
    private void analyseResult(MulticastResult results, List<String> listAndroidPushtoken) {
        for (int i = 0; i < listAndroidPushtoken.size(); i++) {
            String currentPushToken = listAndroidPushtoken.get(i)
            Result result = results.getResults().get(i)

            // Si le message a été bien transmis
            if (result.getMessageId() != null) {
                String canonicalRegId = result.getCanonicalRegistrationId()
                // Si un token plus récent est fourni alors le remplacer
                if (canonicalRegId != null) {
                    List<User> listUser = User.findAll("from User usr where :token in elements(usr.androidPushTokens)", [token: currentPushToken])
                    for (User user : listUser) {
                        user.androidPushTokens.remove(currentPushToken)
                        user.androidPushTokens.add(canonicalRegId);
                    }

                }
            }
            // Si le message n'a pas été transmis
            else {
                String error = result.getErrorCodeName();
                // L'application c'est désenregister : retirer le token
                if (Constants.ERROR_NOT_REGISTERED.equals(error)) {
                    List<User> listUser = User.findAll("from User usr where :token in elements(usr.androidPushTokens)", [token: currentPushToken])
                    for (User user : listUser) {
                        user.androidPushTokens.remove(currentPushToken)
                    }
                }
            }
        }
    }
}
