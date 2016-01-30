package it.uspread.core.domain

/**
 * Modèle destiné à stocker les images dans une table dédié
 */
class Image {

    /** Taille maximale d'une image en "caractère Base64 compressé" */
    public static final int IMAGE_MAX_SIZE = 300000

    /** Image de fond au fromat JPG (Compression 80) encodé en Base64 et enfin zippé */
    String image

    static belongsTo = Message

    static mapping = {
        version(false)
        table('image')
        id([generator:'sequence', params:[sequence:'image_sequence']])
        image(length: IMAGE_MAX_SIZE)
    }

    static constraints = {
        image(maxsize: IMAGE_MAX_SIZE)
    }

}
