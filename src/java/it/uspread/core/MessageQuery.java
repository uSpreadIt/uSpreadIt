package it.uspread.core;

/**
 * Type de requete sur les messages.
 */
public enum MessageQuery {
	/** Messages de l'utilisateur */
    AUTHOR,
    /** Messages reçus par l'utilisateur */
    RECEIVED,
    /** Messages propagé par l'utilisateur */
    SPREAD;
}
