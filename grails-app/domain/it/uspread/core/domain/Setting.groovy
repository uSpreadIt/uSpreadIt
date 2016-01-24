package it.uspread.core.domain

/**
 * Modèle destiné à stocker les paramètres de l'application
 */
class Setting {

    /** Nombre maximum d'utilisateur public (ayant le role {@link Role.USER_PUBLIC}) */
    long maxUser
    /** Nombre de message reçus maximum pour un utilisateur */
    int maxReceivedMessageByUser
    /** Nombre de message de type {@link MessageType.WORLD} qu'un utilisateur peut créer par jour */
    int maxCreateWorldMessageByDayForUser
    /** Nombre de message de type {@link MessageType.WORLD} qu'un utilisateur "Premium" peut créer par jour */
    int maxCreateWorldMessageByDayForPremiumUser
    /** Nombre de message de type {@link MessageType.LOCAL} qu'un utilisateur peut créer par jour */
    int maxCreateLocalMessageByDayForUser
    /** Nombre de message de type {@link MessageType.LOCAL} qu'un utilisateur "Premium" peut créer par jour */
    int maxCreateLocalMessageByDayForPremiumUser
    /** Le nombre de propagation lancé pour un message de type {@link MessageType.WORLD} qui viens d'être créé */
    int nbUserForInitialWorldSpread
    /** Le nombre de propagation lancé pour un message de type {@link MessageType.WORLD} */
    int nbUserForWorldSpread
    /** Le nombre de propagation lancé pour un message de type {@link MessageType.LOCAL} qui viens d'être créé */
    int nbUserForInitialLocalSpread
    /** Le nombre de propagation lancé pour un message de type {@link MessageType.LOCAL} */
    int nbUserForLocalSpread

    static mapping = {
        version(true)
        cache(usage: 'nonstrict-read-write')
        table('setting')
        id([generator:'sequence', params:[sequence:'setting_sequence']])
    }

    static constraints = {
        maxUser(min: 10L)
        maxReceivedMessageByUser(min: 1)
        maxCreateWorldMessageByDayForUser(min: 1)
        maxCreateWorldMessageByDayForPremiumUser(min: 1)
        maxCreateLocalMessageByDayForUser(min: 1)
        maxCreateLocalMessageByDayForPremiumUser(min: 1)
        nbUserForInitialWorldSpread(min: 2)
        nbUserForWorldSpread(min: 2)
        nbUserForInitialLocalSpread(min: 2)
        nbUserForLocalSpread(min: 2)
    }
}
