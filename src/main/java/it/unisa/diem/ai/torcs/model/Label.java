package it.unisa.diem.ai.torcs.model;

/**
 * Enum che rappresenta le possibili etichette (azioni discrete) per un campione {@link Sample}.
 * Utilizzato per la classificazione supervisionata del comportamento di guida.
 */
public enum Label {

    /** Sterzata a sinistra (steering positivo) */
    GIRA_SINISTRA(0),

    /** Accelerazione forte */
    ACCELERA(1),

    /** Sterzata a destra (steering negativo) */
    GIRA_DESTRA(2),

    /** Frenata attiva */
    FRENA(3),

    /** Retromarcia attiva */
    RETROMARCIA(4),

    /** Decelerazione o guida neutra */
    DECELERAZIONE(5);

    /** Codice numerico associato alla label (utile per CSV o classificatori) */
    private final int code;

    /**
     * Costruttore con codice numerico.
     *
     * @param code intero associato alla label
     */
    Label(int code) {
        this.code = code;
    }

    /**
     * Restituisce il codice numerico associato alla label.
     *
     * @return codice intero
     */
    public int getCode() {
        return code;
    }

    /**
     * Ricostruisce una label a partire dal suo codice numerico.
     *
     * @param code codice intero
     * @return {@link Label} corrispondente
     * @throws IllegalArgumentException se il codice non è valido
     */
    public static Label fromCode(int code) {
        for (Label l : values()) {
            if (l.code == code) return l;
        }
        throw new IllegalArgumentException("Codice label non valido: " + code);
    }

    /**
     * Discretizza un'azione continua in una label simbolica, secondo priorità:
     * <ol>
     *     <li>Retromarcia (gear = -1 e accelerazione > 0)</li>
     *     <li>Frenata (brake > 0.1)</li>
     *     <li>Sterzata sinistra (steering >= 0.10)</li>
     *     <li>Sterzata destra (steering <= -0.10)</li>
     *     <li>Accelerazione (accelerate >= 0.9)</li>
     *     <li>Altrimenti: Decelerazione</li>
     * </ol>
     *
     * @param action l'oggetto {@link Action} da discretizzare
     * @return la {@link Label} corrispondente
     */
    public static Label fromAction(Action action) {
        if (action.gear == -1 && action.accelerate > 0)
            return RETROMARCIA;

        if (action.brake > 0.1f)
            return FRENA;

        if (action.steering >= 0.10f)
            return GIRA_SINISTRA;

        if (action.steering <= -0.10f)
            return GIRA_DESTRA;

        if (action.accelerate >= 0.9f)
            return ACCELERA;

        return DECELERAZIONE;
    }

    /**
     * Restituisce una stringa leggibile per la label.
     *
     * @return descrizione in italiano dell'azione
     */
    @Override
    public String toString() {
        return switch (this) {
            case GIRA_SINISTRA -> "Gira Sinistra";
            case ACCELERA -> "Accelera";
            case GIRA_DESTRA -> "Gira Destra";
            case FRENA -> "Frena";
            case RETROMARCIA -> "Retromarcia";
            case DECELERAZIONE -> "Decelerazione";
        };
    }
}
