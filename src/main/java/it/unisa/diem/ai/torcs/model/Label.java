package it.unisa.diem.ai.torcs.model;

public enum Label {
    GIRA_SINISTRA(0),
    ACCELERA(1),
    GIRA_DESTRA(2),
    FRENA(3),
    RETROMARCIA(4),
    DECELERAZIONE(5);

    private final int code;
    Label(int code) { this.code = code; }
    public int getCode() { return code; }

    public static Label fromCode(int code) {
        for (Label l : values()) if (l.code == code) return l;
        throw new IllegalArgumentException("Codice label non valido: " + code);
    }

    // Metodo statico per la discretizzazione, ottengo una label a partire da un'azione
    public static Label fromAction(Action action) {
        // Priorità 1: retromarcia
        if (action.gear == -1 && action.accelerate > 0)
            return RETROMARCIA;

        // Priorità 2: frenata
        if (action.brake > 0.2f)
            return FRENA;

        if (action.steering >= 0.2f)
            return GIRA_SINISTRA;

        if (action.steering <= -0.2f)
            return GIRA_DESTRA;

        // Priorità 4: accelerazione forte
        if (action.accelerate >= 0.7f)
            return ACCELERA;


        // Fallback: DECELERAZIONE (bassa accelerazione, sterzo neutro)
        return DECELERAZIONE;
    }


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
