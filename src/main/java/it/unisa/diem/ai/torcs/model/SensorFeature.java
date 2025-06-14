package it.unisa.diem.ai.torcs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Enumerazione che rappresenta un sottoinsieme di sensori selezionati dal modello TORCS.
 * Ogni valore rappresenta una specifica feature che può essere usata per classificazione o apprendimento automatico.
 */
public enum SensorFeature {

    /** Velocità lungo l'asse X del veicolo */
    SPEED_X("Speed X"),

    /** Velocità laterale lungo l'asse Y del veicolo */
    SPEED_Y("Speed Y"),

    /** Angolo tra l'asse del veicolo e l'asse centrale della pista */
    ANGLE_TO_TRACK_AXIS("Angle to Track Axis"),

    /** Posizione laterale sulla pista rispetto al centro (range [-1, 1]) */
    TRACK_POSITION("Track Position"),

    /** Sensore di distanza al bordo della pista in posizione 4 (tra -90° e +90°) */
    TRACK_EDGE_SENSOR_4("Track Edge Sensor 4", 4),

    /** Sensore di distanza al bordo della pista in posizione 6 */
    TRACK_EDGE_SENSOR_6("Track Edge Sensor 6", 6),

    /** Sensore di distanza al bordo della pista in posizione 8 */
    TRACK_EDGE_SENSOR_8("Track Edge Sensor 8", 8),

    /** Sensore di distanza al bordo della pista in posizione 9 */
    TRACK_EDGE_SENSOR_9("Track Edge Sensor 9", 9),

    /** Sensore di distanza al bordo della pista in posizione 10 */
    TRACK_EDGE_SENSOR_10("Track Edge Sensor 10", 10),

    /** Sensore di distanza al bordo della pista in posizione 12 */
    TRACK_EDGE_SENSOR_12("Track Edge Sensor 12", 12),

    /** Sensore di distanza al bordo della pista in posizione 14 */
    TRACK_EDGE_SENSOR_14("Track Edge Sensor 14", 14);

    private final String displayName;
    private final Integer trackSensorIndex;

    /**
     * Costruttore per sensori senza indice associato (es. velocità, angolo, posizione).
     * @param displayName nome da mostrare
     */
    SensorFeature(String displayName) {
        this.displayName = displayName;
        this.trackSensorIndex = null;
    }

    /**
     * Costruttore per sensori di bordo pista con indice.
     * @param displayName nome da mostrare
     * @param trackSensorIndex indice nell'array dei sensori di bordo pista
     */
    SensorFeature(String displayName, int trackSensorIndex) {
        this.displayName = displayName;
        this.trackSensorIndex = trackSensorIndex;
    }

    /**
     * @return nome leggibile della feature
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * @return indice del sensore di bordo pista (se applicabile), altrimenti {@code null}
     */
    public Integer getTrackSensorIndex() {
        return trackSensorIndex;
    }

    /**
     * Restituisce gli indici dei soli sensori di bordo pista utilizzati.
     * @return lista di interi corrispondenti agli indici nel vettore di sensori "track"
     */
    public static List<Integer> getTrackSensorIndices() {
        List<Integer> indices = new ArrayList<>();
        for (SensorFeature feature : SensorFeature.values()) {
            if (feature.trackSensorIndex != null) {
                indices.add(feature.trackSensorIndex);
            }
        }
        return indices;
    }

    /**
     * Costruisce l'intestazione CSV completa con i nomi delle feature + LABEL_CODE e LABEL_NAME.
     * @return stringa da usare come intestazione di un file CSV
     */
    public static String csvHeader() {
        StringBuilder sb = new StringBuilder();
        for (SensorFeature f : SensorFeature.values()) {
            sb.append(f.name()).append(";");
        }
        sb.append("LABEL_CODE").append(";").append("LABEL_NAME");
        return sb.toString();
    }
}
