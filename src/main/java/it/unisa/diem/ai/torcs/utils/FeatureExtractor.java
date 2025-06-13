package it.unisa.diem.ai.torcs.utils;

import it.unisa.diem.ai.torcs.model.Feature;
import it.unisa.diem.ai.torcs.model.SensorModel;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.valueOf;

public class FeatureExtractor {
    private static final int[] TRACK_SENSOR_INDICES = {4,6,8,9,10,12,14};

    public Feature extractFeatures(SensorModel sensors) {
        List<Double> features = new ArrayList<>();
        double[] trackSensors = sensors.getTrackEdgeSensors();

        features.add(valueOf(sensors.getSpeed()));
        features.add(valueOf(sensors.getAngleToTrackAxis()));
        features.add(valueOf(sensors.getTrackPosition()));

        for (int idx : TRACK_SENSOR_INDICES) {
            features.add(valueOf(trackSensors[idx]));
        }
        return new Feature(features);
    }
}
