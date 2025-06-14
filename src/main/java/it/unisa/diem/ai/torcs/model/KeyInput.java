package it.unisa.diem.ai.torcs.model;

/**
 * Classe statica per tenere traccia dello stato dei tasti premuti in tempo reale.
 * Viene aggiornata dinamicamente dalla GUI {@link it.unisa.diem.ai.torcs.io.ContinuousCharReaderUI}
 * e utilizzata da controller manuali come {@code HumanDriver}.
 */
public class KeyInput {

    /** Stato del tasto 'W' (accelerazione premuta = true) */
    public static boolean up = false;

    /** Stato del tasto 'S' (retromarcia premuta = true) */
    public static boolean down = false;

    /** Stato del tasto 'A' (sterzata a sinistra premuta = true) */
    public static boolean left = false;

    /** Stato del tasto 'D' (sterzata a destra premuta = true) */
    public static boolean right = false;

    /** Stato del tasto 'Spazio' (freno premuto = true) */
    public static boolean brake = false;
}
