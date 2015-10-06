package it.uspread.core.params

/**
 * Critères de recherche des messages
 */
class MessageCriteria {

    /** Nombre d'éléments à retourner */
    long count
    /** Indique une date sur laquelle se baser pour séléctionner les messages : utilisation conjointe avec {@link #operator} */
    Date date
    /** Indique l'opérateur utilisé pour effectuer la comparaison de date (cf {@link QueryParams}) :  utilisation conjointe avec {@link #date} */
    String operator

    MessageCriteria(String count, String date, String operator) {
        this.count = count != null ? new Long(count) : 0
        this.date = date != null ? QueryParams.DATE_FORMAT.parse(date) : null
        this.operator = operator
    }
}
