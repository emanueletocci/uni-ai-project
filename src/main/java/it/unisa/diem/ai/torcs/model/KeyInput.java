package it.unisa.diem.ai.torcs.model;

/**
 * Classe statica per tenere traccia dei tasti premuti in tempo reale.
 * Usata da ContinuousCharReaderUI e dal driver (es. HumanDriver).
 */
public class KeyInput {
    public static boolean up = false;     // 'W'
    public static boolean down = false;   // 'S'
    public static boolean left = false;   // 'A'
    public static boolean right = false;  // 'D'
    public static boolean brake = false;  // 'Space'
}