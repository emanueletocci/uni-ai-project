package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.sensors.SensorModel;

public class AutonomousDriverUtility {
    private final NearestNeighbor knn;
    private final Action action;

    // Costanti per la normalizzazione
    private static final double MAX_TRACK_VALUE = 200.0;
    private static final double MAX_SPEED = 300.0;
    private static final double STEER_LIMIT = 0.8;
    private static final int[] SENSOR_INDICES = {0, 3, 4, 8, 9, 10, 14, 15, 18};

    public AutonomousDriverUtility(String datasetPath) {
        knn = new NearestNeighbor(datasetPath);
        action = new Action();
    }

    public Action decide(SensorModel sensors, int gear) {
        double[] features = extractAndNormalizeFeatures(sensors);
        int predictedClass = knn.classify(new Sample(features), 5);

        applyAction(predictedClass, sensors.getSpeed());
        handleGearAndSafety(gear, sensors.getTrackPosition());

        return action;
    }

    private double[] extractAndNormalizeFeatures(SensorModel sensors) {
        double[] features = new double[12];
        double[] track = sensors.getTrackEdgeSensors();

        // Normalizzazione sensori di bordo pista
        for(int i = 0; i < SENSOR_INDICES.length; i++) {
            double value = Math.max(0, track[SENSOR_INDICES[i]]);
            features[i] = value / MAX_TRACK_VALUE;
        }

        // Altre feature
        features[9] = sensors.getTrackPosition();  // Già normalizzato [-1,1]
        features[10] = sensors.getAngleToTrackAxis() / Math.PI;
        features[11] = sensors.getSpeed() / MAX_SPEED;

        return features;
    }

    private void applyAction(int predictedClass, double currentSpeed) {
        double speedFactor = 1.0 - (currentSpeed / MAX_SPEED);

        switch(predictedClass) {
            case 0: fullAcceleration(); break;
            case 1: sharpLeft(speedFactor); break;
            case 2: moderateLeft(speedFactor); break;
            case 3: gentleLeft(speedFactor); break;
            case 4: sharpRight(speedFactor); break;
            case 5: moderateRight(speedFactor); break;
            case 6: gentleRight(speedFactor); break;
            case 7: fullBrake(); break;
            case 8: reverse(); break;
            default: safeDeceleration(); break;
        }
    }

    private void handleGearAndSafety(int gear, double trackPos) {
        action.gear = gear;

        // Safety override per posizioni estreme
        if(Math.abs(trackPos) > 0.9) {
            action.accelerate = 0.1;
            action.steering = (trackPos > 0) ? -0.5 : 0.5;
        }
    }

    // Azioni di guida con parametrizzazione dinamica
    private void fullAcceleration() {
        action.accelerate = 1.0;
        action.brake = 0.0;
        action.steering = 0.0;
    }

    private void sharpLeft(double speedFactor) {
        action.accelerate = 0.7 * speedFactor;
        action.steering = STEER_LIMIT;
    }

    private void moderateLeft(double speedFactor) {
        action.accelerate = 0.8 * speedFactor;
        action.steering = 0.5;
    }

    private void gentleLeft(double speedFactor) {
        action.accelerate = speedFactor;
        action.steering = 0.2;
    }

    private void sharpRight(double speedFactor) {
        action.accelerate = 0.7 * speedFactor;
        action.steering = -STEER_LIMIT;
    }

    private void moderateRight(double speedFactor) {
        action.accelerate = 0.8 * speedFactor;
        action.steering = -0.5;
    }

    private void gentleRight(double speedFactor) {
        action.accelerate = speedFactor;
        action.steering = -0.2;
    }

    private void fullBrake() {
        action.accelerate = 0.0;
        action.brake = 1.0;
        action.steering = 0.0;
    }

    private void reverse() {
        action.gear = -1;
        action.accelerate = 0.3;
    }

    private void safeDeceleration() {
        action.accelerate = 0.0;
        action.brake = 0.2;  // Frenata leggera di sicurezza
    }
}
