package it.unisa.diem.ai.torcs.utils;

import it.unisa.diem.ai.torcs.model.FeatureVector;
import it.unisa.diem.ai.torcs.model.SensorModel;
import it.unisa.diem.ai.torcs.model.SensorFeature;

import java.util.ArrayList;
import java.util.List;

public class FeatureExtractor {

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
                    // Se è un sensore di bordo pista, usa il relativo indice
                    Integer idx = feature.getTrackSensorIndex();
                    if (idx != null) {
                        features.add(trackSensors[idx]);
                    }
            }
        }
        return new FeatureVector(features);
    }
}
