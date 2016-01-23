package it.uspread.core.data

/**
 * Informations du statut de l'utiliateur
 */
class Status {

    boolean worldQuotaReached
    boolean localQuotaReached
    long nbMessageWrited
    long nbMessageSpread
    long nbMessageIgnored
    long nbMessageReported
}
