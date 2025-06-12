package it.unisa.diem.ai.torcs;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import it.unisa.diem.ai.torcs.model.FeatureType;

/**
 * Sample class represents a single data point in the dataset.
 * It contains features and a class label, which can be used for classification tasks.
 * The class also provides methods to calculate the distance between samples.
 */
public class Sample {
    private final Map<FeatureType, Double> featuresMap;
   public int dataClass;
    public Sample() {
        featuresMap = new HashMap<>();
    }

    private Sample(Map<FeatureType, Double> featuresMap) {
        this.featuresMap = featuresMap;
    }

    public void set(FeatureType type, double value) {
        featuresMap.put(type, value);
    }

    public double get(FeatureType type) {
        return featuresMap.getOrDefault(type, 0d);
    }

    public double get(int index) {
        return featuresMap.getOrDefault(FeatureType.values()[index], 0d);
    }

    public void normalize(FeatureType type, double min, double max) {
        double value = featuresMap.get(type);
        featuresMap.put(type, (value - min) / (max - min));
    }

    public double euclideanDistance(Sample o) {
        return Math.sqrt(
                Math.pow(get(FeatureType.SPEED) - o.get(FeatureType.SPEED), 2) +
                Math.pow(get(FeatureType.ANGLE_TO_TRACK_AXIS) - o.get(FeatureType.ANGLE_TO_TRACK_AXIS), 2) +
                Math.pow(get(FeatureType.TRACK_POSITION) - o.get(FeatureType.TRACK_POSITION), 2) +
                Math.pow(get(FeatureType.TRACK_EDGE_SENSORS_4) - o.get(FeatureType.TRACK_EDGE_SENSORS_4), 2) +
                Math.pow(get(FeatureType.TRACK_EDGE_SENSORS_6) - o.get(FeatureType.TRACK_EDGE_SENSORS_6), 2) +
                Math.pow(get(FeatureType.TRACK_EDGE_SENSORS_8) - o.get(FeatureType.TRACK_EDGE_SENSORS_8), 2) +
                Math.pow(get(FeatureType.TRACK_EDGE_SENSORS_9) - o.get(FeatureType.TRACK_EDGE_SENSORS_9), 2) +
                Math.pow(get(FeatureType.TRACK_EDGE_SENSORS_10) - o.get(FeatureType.TRACK_EDGE_SENSORS_10), 2) +
                Math.pow(get(FeatureType.TRACK_EDGE_SENSORS_12) - o.get(FeatureType.TRACK_EDGE_SENSORS_12), 2) +
                Math.pow(get(FeatureType.TRACK_EDGE_SENSORS_14) - o.get(FeatureType.TRACK_EDGE_SENSORS_14), 2)
        );
    }

    public static Sample fromString(String line) {
        Sample dataSample = new Sample();
        String[] parts = line.split(";");
        dataSample.set(FeatureType.SPEED, Double.parseDouble(parts[0].trim()));
        dataSample.set(FeatureType.ANGLE_TO_TRACK_AXIS, Double.parseDouble(parts[1].trim()));
        dataSample.set(FeatureType.TRACK_POSITION, Double.parseDouble(parts[2].trim()));
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_4, Double.parseDouble(parts[3].trim()));
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_6, Double.parseDouble(parts[4].trim()));
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_8, Double.parseDouble(parts[5].trim()));
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_9, Double.parseDouble(parts[6].trim()));
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_10, Double.parseDouble(parts[7].trim()));
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_12, Double.parseDouble(parts[8].trim()));
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_14, Double.parseDouble(parts[9].trim()));
        dataSample.dataClass = Integer.parseInt(parts[10].trim());
        return dataSample;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(";");
        joiner.add(String.valueOf(get(FeatureType.SPEED)));
        joiner.add(String.valueOf(get(FeatureType.ANGLE_TO_TRACK_AXIS)));
        joiner.add(String.valueOf(get(FeatureType.TRACK_POSITION)));
        joiner.add(String.valueOf(get(FeatureType.TRACK_EDGE_SENSORS_4)));
        joiner.add(String.valueOf(get(FeatureType.TRACK_EDGE_SENSORS_6)));
        joiner.add(String.valueOf(get(FeatureType.TRACK_EDGE_SENSORS_8)));
        joiner.add(String.valueOf(get(FeatureType.TRACK_EDGE_SENSORS_9)));
        joiner.add(String.valueOf(get(FeatureType.TRACK_EDGE_SENSORS_10)));
        joiner.add(String.valueOf(get(FeatureType.TRACK_EDGE_SENSORS_12)));
        joiner.add(String.valueOf(get(FeatureType.TRACK_EDGE_SENSORS_14)));
        joiner.add(String.valueOf(dataClass));
        return joiner.toString();
    }
}
