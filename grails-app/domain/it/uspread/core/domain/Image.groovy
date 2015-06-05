package it.uspread.core.domain

/**
 * Modèle destiné à stocker les images dans une table dédié
 */
class Image {

    /** Taille maximale d'une image en octet */
    public static final int IMAGE_MAX_SIZE = 1048576

    /** Image de fond encodé en PNG */
    byte[] image

    static belongsTo = Message

    static mapping = {
        version(false)
        id([generator:'sequence', params:[sequence:'image_sequence']])
    }

    static constraints = {
        image(maxSize: IMAGE_MAX_SIZE)
    }

}
