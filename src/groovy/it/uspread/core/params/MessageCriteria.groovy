package it.uspread.core.params

class MessageCriteria {

    /** Nombre d'éléments à retourner */
    long count
    /** Indique de retourner que les messages plus anciens que cette date */
    Date beforeDate
    /** Indique de retourner que les messages plus récent que cette date */
    Date afterDate

    MessageCriteria(String count, String date, String operator) {
        this.count = count != null ? new Long(count) : 0
        Date dateCriteria = date != null ? QueryParams.DATE_FORMAT.parse(date.replace("%2B", "+")) : null
        if (QueryParams.BEFORE.equals(operator)) {
            beforeDate = dateCriteria
        } else if (QueryParams.AFTER.equals(operator)) {
            afterDate = dateCriteria
        }
    }
}
