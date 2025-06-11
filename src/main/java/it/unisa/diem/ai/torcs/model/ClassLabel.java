package it.unisa.diem.ai.torcs.model;
import it.unisa.diem.ai.torcs.actions.Action;

/**
 * Gestione centralizzata delle classi di azione per behavioral cloning e classificazione.
 */
public enum ClassLabel {
    ACCELERA_DRITTO(0, "acceleraDritto"),
    GIRA_LEGGERO_SINISTRA(1, "giraLeggeroSinistra"),
    GIRA_FORTE_SINISTRA(2, "giraForteSinistra"),
    GIRA_LEGGERO_DESTRA(3, "giraLeggeroDestra"),
    GIRA_FORTE_DESTRA(4, "giraForteDestra"),
    FRENA(5, "frena"),
    RETROMARCIA(6, "retromarcia"),
    MANTIENI_VELOCITA(7, "mantieniVelocita");

    private final int code;
    private final String label;

    ClassLabel(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ClassLabel calculateLabel(Action action) {
        if (action.gear == -1 && action.accelerate > 0) return RETROMARCIA;
        if (action.brake > 0.1) return FRENA;
        if (action.steering >= 0.6f) return GIRA_FORTE_SINISTRA;
        if (action.steering >= 0.15f) return GIRA_LEGGERO_SINISTRA;
        if (action.steering <= -0.6f) return GIRA_FORTE_DESTRA;
        if (action.steering <= -0.15f) return GIRA_LEGGERO_DESTRA;
        if (action.accelerate > 0.7f) return ACCELERA_DRITTO;
        return MANTIENI_VELOCITA;
    }

    public static ClassLabel fromCode(int code) {
        for (ClassLabel cl : values()) {
            if (cl.code == code) return cl;
        }
        throw new IllegalArgumentException("Classe non valida: " + code);
    }
}


