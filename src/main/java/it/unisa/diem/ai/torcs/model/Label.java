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
        // Priorità 1: Retromarcia
        if (action.gear == -1) {
            return RETROMARCIA;
        }

        // Priorità 2: Frenata
        if (action.brake > 0.3) {
            return FRENA;
        }

        // Priorità 3: Sterzate
        if (action.steering < -0.1) {
            return GIRA_SINISTRA;
        }
        if (action.steering > 0.1) {
            return GIRA_DESTRA;
        }

        // Priorità 4: Accelerazione sostenuta
        if (action.accelerate > 0.7) {
            return ACCELERA;
        }

        // Altrimenti avanti dritto
        return DECELERAZIONE;
    }

    @Override
    public String toString() {
        return switch (this) {
            case GIRA_SINISTRA -> "Avanti Sinistra";
            case ACCELERA -> "Avanti Dritto";
            case GIRA_DESTRA -> "Avanti Destra";
            case FRENA -> "Frena";
            case RETROMARCIA -> "Retromarcia";
            case DECELERAZIONE -> "Decelerazione";
        };
    }
}
