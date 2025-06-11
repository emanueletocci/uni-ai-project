package it.unisa.diem.ai.torcs.model;
import it.unisa.diem.ai.torcs.actions.Action;

/**
 * Enum che centralizza la gestione delle classi di azione per behavioral cloning e classificazione.
 * Ogni valore enum rappresenta una possibile azione di alto livello eseguibile dal veicolo.
 */
public enum ClassLabel {
    /**
     * Azione: accelera dritto.
     */
    ACCELERA_DRITTO(0, "acceleraDritto"),
    /**
     * Azione: gira a sinistra.
     */
    GIRA_SINISTRA(1, "giraSinistra"),

    /**
     * Azione: gira a destra.
     */
    GIRA_DESTRA(2, "giraDestra"),

    /**
     * Azione: mantieni velocità (nessuna azione specifica).
     */
    MANTIENI_VELOCITA(3, "mantieniVelocita"),
    /**
     * Azione: retromarcia.
     */
    RETROMARCIA(4, "retromarcia"),

    /**
     * Azione: frena.
     */
    FRENA(5, "frena");


    private final int code;
    private final String label;

    /**
     * Costruttore dell'enum.
     * @param code Codice numerico della classe.
     * @param label Nome descrittivo della classe.
     */
    ClassLabel(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * Restituisce il codice numerico associato alla classe.
     * @return codice numerico della classe.
     */
    public int getCode() {
        return code;
    }

    /**
     * Restituisce il nome descrittivo della classe.
     * @return nome della classe.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Calcola la label di classe a partire da un oggetto Action.
     * La logica di assegnazione è centralizzata e coerente con la definizione delle classi.
     * @param action Azione da classificare.
     * @return ClassLabel corrispondente all'azione.
     */
    public static ClassLabel calculateLabel(Action action) {
        if (action.gear == -1 && action.accelerate > 0) return RETROMARCIA;
        if (action.brake > 0.1) return FRENA;
        if (action.steering > 0.15) return GIRA_SINISTRA;
        if (action.steering < -0.15) return GIRA_DESTRA;
        if (action.accelerate > 0.7f) return ACCELERA_DRITTO;
        return MANTIENI_VELOCITA;
    }

    /**
     * Restituisce la classe corrispondente al codice numerico fornito.
     * Utile per convertire label numeriche (ad esempio lette da file o da un classificatore)
     * nell'enum corrispondente.
     * @param code Codice numerico della classe.
     * @return ClassLabel corrispondente.
     * @throws IllegalArgumentException se il codice non corrisponde a nessuna classe.
     */
    public static ClassLabel fromCode(int code) {
        for (ClassLabel cl : values()) {
            if (cl.code == code) return cl;
        }
        throw new IllegalArgumentException("Classe non valida: " + code);
    }
}
