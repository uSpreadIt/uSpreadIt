package it.uspread.core.domain

/**
 * Modèle destiné à stocker les images dans une table dédié
 */
class Image {

    /** Image de fond encodé en PNG */
    byte[] image

    static belongsTo = Message

    static mapping = {
        version(false)
        id([generator:'sequence', params:[sequence:'image_sequence']])
        image(type: 'blob')
    }

    static constraints = {
    }
}
