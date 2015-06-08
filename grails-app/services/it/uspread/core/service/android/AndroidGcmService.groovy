package it.uspread.core.service.android

import grails.transaction.Transactional
import it.uspread.core.domain.User
import it.uspread.core.json.JSONAttribute

import com.google.android.gcm.server.Constants
import com.google.android.gcm.server.MulticastResult
import com.google.android.gcm.server.Result
import com.google.android.gcm.server.Sender

/**
 * Service du système de notification PUSH Android.
 * FIXME optimisation des requetes car là c'est pas ok sauf si Grails est super bon pour comprendre
 */
@Transactional
class AndroidGcmService {

    /** Api Key Android GCM */
    public static final String API_KEY = "AIzaSyBi4Hdxi28HlbtZ8RKhlF2SYumzcHPJukU"
    /** Nombre de seconde avant d'abandonner le stockage en attente de transmission d'un message ayant un collapseKey (send-to-sync) : 1 semaine */
    public static final int TIME_TO_LIVE = 604800
    /** Indique que le message ne nécessite pas d'être envoyé immédiatement si le terminal est en veille */
    public static final boolean DELAY_WHILE_IDLE = true
    /** Nombre de tentative de transmission du message aux serveurs GCM */
    public static final int RETRY = 3

    /**
     * Lance une notification push pour indiquer aux différents clients android des utilisateurs concernés qu'un nouveau message est disponible.
     * @param listUser Les utilisateurs recevant un nouveau message
     */
    void notifyMessageSentTo(List<User> listUser) {
        for (User user : listUser) {
            Set<String> listAndroidPushtoken = user.androidPushTokens
            if (listAndroidPushtoken) {
                def data = [type: "SYNC", (JSONAttribute.USER_USERNAME): user.username]
                def result = sendMessage(data, listAndroidPushtoken, "New messages ${JSONAttribute.USER_USERNAME}")
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
     * @param message l'id du message qui est supprimé
     */
    void notifyMessageDeleteTo(Set<User> listUser, Long messageId) {
        for (User user : listUser) {
            Set<String>  listAndroidPushtoken = user.androidPushTokens
            if (listAndroidPushtoken) {
                def data = [type: "DELETE", (JSONAttribute.USER_USERNAME): user.username, (JSONAttribute.MESSAGE_ID): messageId.toString()]
                // TODO Quel est la limite de la taille de la collection qui peut etre envoyé
                def result = sendMessage(data, listAndroidPushtoken)
                if (listAndroidPushtoken.size() > 1) {
                    analyseMultiCastResult(result, listAndroidPushtoken)
                } else {
                    analyseResult(result, listAndroidPushtoken.get(0))
                }
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
        List<User> listUser = User.findAll("from User usr where usr.id <> :idUsr and :token in elements(usr.androidPushTokens)", [token: pushToken, idUsr: user.id])
        for (User usr : listUser) {
            usr.removeFromAndroidPushTokens(pushToken)
        }
        // On reaffecte le push token à l'user demandé
        registerPushToken(user, pushToken)
    }

    /** Enregistrement d'un token pour l'utilisateur  (s'il est nouveau) */
    void registerPushToken(User user, String androidPushToken) {
        if (!user.androidPushTokens.contains(androidPushToken)) {
            user.addToAndroidPushTokens(androidPushToken)
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
            if (Constants.ERROR_NOT_REGISTERED == error) {
                List<User> listUser = User.findAll("from User usr where :token in elements(usr.androidPushTokens)", [token: androidPushToken])
                for (User user : listUser) {
                    user.androidPushTokens.remove(androidPushToken)
                }
            }
        }
    }

    /**
     * Envoyer un message (Si une collapse key est fourni alors le message pourra être abandonné au profit du nouveau de même collapse key)<br>
     * Le message sera envoyé à un ou plusieurs périphérique suivant la liste de push token
     * Des données peuvent être ajouté
     * @param collapseKey attention le serveur n'accepte qu'un maximum de 4 messages de collapseKey différents pour un périphérique
     * @return un Result ou un MultiCastResult suivant si 1 ou plusieurs push token sont donné
     */
    private def sendMessage(Map data, Set<String> registrationIds, String collapseKey = '') {
        new Sender(API_KEY).send(buildMessage(data, collapseKey), registrationIds.size() > 1 ? new ArrayList(registrationIds) : registrationIds[0], RETRY)
    }

    private com.google.android.gcm.server.Message buildMessage(Map data, String collapseKey = '') {
        withMessageBuilder(data) { com.google.android.gcm.server.Message.Builder messageBuilder ->
            if (collapseKey) {
                messageBuilder.collapseKey(collapseKey).timeToLive(TIME_TO_LIVE)
            }
        }
    }

    private com.google.android.gcm.server.Message withMessageBuilder(Map messageData, Closure builderConfigurator) {
        com.google.android.gcm.server.Message.Builder messageBuilder = new com.google.android.gcm.server.Message.Builder().delayWhileIdle(DELAY_WHILE_IDLE)
        if (builderConfigurator) {
            builderConfigurator(messageBuilder)
        }
        messageData.each {
            messageBuilder.addData(it.key, it.value)
        }
        messageBuilder.build()
    }
}
