package it.unisa.diem.ai.torcs.model;

public enum Label {
    AVANTI_SINISTRA(0),
    AVANTI_DRITTO(1),
    AVANTI_DESTRA(2),
    FRENA(3),
    RETROMARCIA(4);

    private final int code;
    Label(int code) { this.code = code; }
    public int getCode() { return code; }

    public static Label fromCode(int code) {
        for (Label l : values()) if (l.code == code) return l;
        throw new IllegalArgumentException("Codice label non valido: " + code);
    }

    // Metodo statico per la discretizzazione, ottengo una label a partire da un'azione
    public static Label fromAction(Action action) {
        // Retromarcia ha priorità
        if (action.gear == -1) {
            return RETROMARCIA;
        }
        // Frenata ha priorità su avanti
        if (action.brake > 0.3) {
            return FRENA;
        }
        // Azioni in avanti, distinte per sterzata
        if (action.steering < -0.1) {
            return AVANTI_SINISTRA;
        }
        if (action.steering > 0.1) {
            return AVANTI_DESTRA;
        }
        // Altrimenti avanti dritto
        return AVANTI_DRITTO;
    }

    @Override
    public String toString() {
        return switch (this) {
            case AVANTI_SINISTRA -> "Avanti Sinistra";
            case AVANTI_DRITTO -> "Avanti Dritto";
            case AVANTI_DESTRA -> "Avanti Destra";
            case FRENA -> "Frena";
            case RETROMARCIA -> "Retromarcia";
        };
    }
}
