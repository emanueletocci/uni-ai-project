package it.unisa.diem.ai.torcs.model;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Enum che rappresenta le feature utilizzate per l'estrazione e la normalizzazione
 * dei dati di input per la guida autonoma in TORCS.
 * Include sia le feature generali del veicolo (velocità, angolo, posizione sulla pista)
 * sia i sensori di bordo pista (track edge sensors) selezionati tramite i relativi indici.
 * Centralizza la configurazione delle feature per garantire coerenza tra logger,
 * normalizzatore e agenti di guida.
 */
public enum FeatureType {
    /**
     * Velocità longitudinale del veicolo.
     */
    SPEED,

    /**
     * Angolo tra l'asse longitudinale del veicolo e la tangente alla pista.
     */
    ANGLE_TO_TRACK_AXIS,

    /**
     * Posizione laterale del veicolo rispetto al centro della pista.
     */
    TRACK_POSITION,

    /**
     * Sensore di bordo pista con indice 5 (tipicamente -40 gradi).
     */
    TRACK_EDGE_SENSOR_5(5),

    /**
     * Sensore di bordo pista con indice 6 (tipicamente -30 gradi).
     */
    TRACK_EDGE_SENSOR_6(6),

    /**
     * Sensore di bordo pista con indice 7 (tipicamente -20 gradi).
     */
    TRACK_EDGE_SENSOR_7(7),

    /**
     * Sensore di bordo pista con indice 8 (tipicamente -10 gradi).
     */
    TRACK_EDGE_SENSOR_8(8),

    /**
     * Sensore di bordo pista con indice 9 (tipicamente 0 gradi, diretto davanti all'auto).
     */
    TRACK_EDGE_SENSOR_9(9),

    /**
     * Sensore di bordo pista con indice 10 (tipicamente +10 gradi).
     */
    TRACK_EDGE_SENSOR_10(10),

    /**
     * Sensore di bordo pista con indice 11 (tipicamente +20 gradi).
     */
    TRACK_EDGE_SENSOR_11(11),

    /**
     * Sensore di bordo pista con indice 12 (tipicamente +30 gradi).
     */
    TRACK_EDGE_SENSOR_12(12),

    /**
     * Sensore di bordo pista con indice 13 (tipicamente +40 gradi).
     */
    TRACK_EDGE_SENSOR_13(13);

    private final Integer trackIndex;

    /**
     * Costruttore per le feature senza indice di sensore (feature generali).
     */
    FeatureType() {
        this.trackIndex = null;
    }

    /**
     * Costruttore per le feature relative ai sensori di bordo pista.
     *
     * @param trackIndex indice del sensore di bordo pista nel vettore track[]
     */
    FeatureType(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    /**
     * Restituisce l'indice del sensore di bordo pista associato alla feature,
     * oppure null se la feature non è un sensore di bordo pista.
     *
     * @return indice del sensore di bordo pista, o null se non applicabile
     */
    public Integer getTrackIndex() {
        return trackIndex;
    }

    /**
     * Restituisce la lista degli indici dei sensori di bordo pista selezionati
     * tra le feature di tipo TRACK_EDGE_SENSOR.
     *
     * @return lista di indici interi dei sensori di bordo pista
     */
    public static List<Integer> getTrackSensorIndices() {
        return Arrays.stream(values())
                .filter(f -> f.trackIndex != null)
                .map(FeatureType::getTrackIndex)
                .collect(Collectors.toList());
    }

    /**
     * Genera l'header CSV corrispondente alle feature definite nell'enum,
     * aggiungendo il campo "CLASS" per la label di classificazione.
     *
     * @return stringa contenente l'header CSV separato da punto e virgola
     */
    public static String getCSVHeader() {
        StringJoiner joiner = new StringJoiner(";");
        for (FeatureType f : values()) {
            joiner.add(f.name());
        }
        joiner.add("CLASS");
        return joiner.toString();
    }
}
