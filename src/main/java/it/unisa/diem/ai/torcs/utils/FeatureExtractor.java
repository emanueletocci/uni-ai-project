package it.unisa.diem.ai.torcs.utils;

import it.unisa.diem.ai.torcs.model.FeatureVector;
import it.unisa.diem.ai.torcs.model.SensorModel;
import it.unisa.diem.ai.torcs.model.SensorFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsabile dell'estrazione delle feature (caratteristiche) rilevanti
 * dal modello dei sensori fornito da TORCS.
 * Costruisce un oggetto {@link FeatureVector} basato su un sottoinsieme selezionato
 * di feature definite nell'enum {@link SensorFeature}.
 */
public class FeatureExtractor {

    /**
     * Estrae le feature definite in {@link SensorFeature} a partire da un'istanza di {@link SensorModel}.
     *
     * @param sensors il modello sensoriale da cui leggere i dati (es. velocità, posizione, sensori di bordo pista).
     * @return un {@link FeatureVector} contenente i valori numerici ordinati delle feature selezionate.
     */
    public FeatureVector extractFeatures(SensorModel sensors) {
        List<Double> features = new ArrayList<>();
        double[] trackSensors = sensors.getTrackEdgeSensors();

        for (SensorFeature feature : SensorFeature.values()) {
            switch (feature) {
                case SPEED_X:
                    features.add(sensors.getSpeed());
                    break;
                case SPEED_Y:
                    features.add(sensors.getLateralSpeed());
                    break;
                case ANGLE_TO_TRACK_AXIS:
                    features.add(sensors.getAngleToTrackAxis());
                    break;
                case TRACK_POSITION:
                    features.add(sensors.getTrackPosition());
                    break;
                default:
                    // Se è un sensore di bordo pista, usa l'indice associato nell'array dei sensori
                    Integer idx = feature.getTrackSensorIndex();
                    if (idx != null) {
                        features.add(trackSensors[idx]);
                    }
            }
        }
        return new FeatureVector(features);
    }
}
