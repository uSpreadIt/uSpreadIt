package it.uspread.core.type

/**
 * Type de message.<br>
 * Limité en BD à 5 caractères
 */
enum MessageType {
    /** Message de propagation mondiale */
    WORLD,
    /** Message de propagation locale */
    LOCAL
}
