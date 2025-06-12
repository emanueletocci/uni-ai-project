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
     SPEED,
    ANGLE_TO_TRACK_AXIS,
    TRACK_POSITION,
    TRACK_EDGE_SENSORS_4,
    TRACK_EDGE_SENSORS_6,
    TRACK_EDGE_SENSORS_8,
    TRACK_EDGE_SENSORS_9,
    TRACK_EDGE_SENSORS_10,
    TRACK_EDGE_SENSORS_12,
    TRACK_EDGE_SENSORS_14;

    public static String getCSVHeader() {
        StringJoiner joiner = new StringJoiner(";");

        for (int i = 0; i < FeatureType.values().length; i++) {
            joiner.add(FeatureType.values()[i].name());
        }

        joiner.add("CLASS");

        return joiner.toString();
    }

}
