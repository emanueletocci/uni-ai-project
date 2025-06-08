package it.unisa.diem.ai.torcs.utilities;

/**
 * Classe statica per tenere traccia dei tasti premuti in tempo reale.
 * Usata da ContinuousCharReaderUI e dal driver (es. HumanDriver).
 */
public class aKeyInput {
    public static boolean up = false;     // 'W'
    public static boolean down = false;   // 'S'
    public static boolean left = false;   // 'A'
    public static boolean right = false;  // 'D'

    //debug disperato
    public static void print() {
        System.out.println("W: " + up + " | A: " + left + " | S: " + down + " | D: " + right);
    }
    static {
        System.out.println("✅ KeyInput loaded by: " + KeyInput.class.getClassLoader());
    }
}