package it.uspread.android

import grails.transaction.Transactional
import it.uspread.core.Message
import it.uspread.core.User
import it.uspread.core.json.JSONAttribute

@Transactional
class AndroidGcmMessageService {

    def androidGcmService
    def grailsApplication

    /**
     * Lance une notification push pour indiquer aux différents clients android des utilisateurs concernés que un ou plusieurs nouveau messages sont disponible
     * @param recipients Les utilisateurs recevant un nouveau message
     * @return
     */
    def notifyMessageSentTo(List<User> recipients) {
        for (User user : recipients) {
            List<String> listAndroidPushtoken = user.androidPushTokens.collect()
            if (listAndroidPushtoken != null && !listAndroidPushtoken.isEmpty()) {
                androidGcmService.sendMulticastCollapseMessage("New Messages", null, listAndroidPushtoken)
                // TODO exploiter le retour pour supprimer les token devenu invalide
            }
        }
    }

    /**
     * Lance une notification push pour indiquer aux différents clients android des utilisateurs concernés qu'un message a été supprimé
     * @param recipients les utilisateurs possédant ce message
     * @param message le message a supprimer
     * @return
     */
    def notifyMessageDeleteTo(List<User> recipients, Message message) {
        for (User user : recipients) {
            List<String> listAndroidPushtoken = user.androidPushTokens.collect()
            if (listAndroidPushtoken != null && !listAndroidPushtoken.isEmpty()) {
                def data = ["${JSONAttribute.MESSAGE_ID}": message.id]
                androidGcmService.sendMulticastInstantMessage(data, listAndroidPushtoken)
                // TODO exploiter le retour pour supprimer les token devenu invalide
            }
        }
    }
}
