package it.uspread.core.type

/**
 * Type du signalement d'un message.<br>
 * Limité en BD à 15 caractères
 */
enum ReportType {
    /** Spam */
    SPAM,
    /** Contenu inapproprié */
    INAPPROPRIATE,
    /** Menace */
    THREAT
}
