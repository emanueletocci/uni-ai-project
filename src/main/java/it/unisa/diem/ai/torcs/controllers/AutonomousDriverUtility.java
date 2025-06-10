package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;

public class AutonomousDriverUtility {
    private final NearestNeighbor knn;
    private final Action action;

    private double angle, speed, position;
    private double[] trackEdgeSensors;

    public AutonomousDriverUtility(String datasetPath) {
        knn = new NearestNeighbor(datasetPath);
        action = new Action();
    }

    public Action decide(SensorModel sensors, int gear) {
        angle = sensors.getAngleToTrackAxis();
        position = sensors.getTrackPosition();
        speed = sensors.getSpeed();
        trackEdgeSensors = sensors.getTrackEdgeSensors();

        double[] features = FeatureNormalizer.extractAndNormalizeFeatures(
                trackEdgeSensors,
                position,
                angle,
                speed
        );

        int predictedClass = knn.classify(new Sample(features), 5);

        applyAction(predictedClass);
        handleGearAndSafety(gear, sensors.getTrackPosition());

        return action;
    }


    private void applyAction(int predictedClass) {

        switch(predictedClass) {
            case 0: accelera(); break;
            case 1: giraSXMolto(); break;
            case 2: giraSX(); break;
            case 3: giraSXPoco(); break;
            case 4: giraDXMolto(); break;
            case 5: giraDX(); break;
            case 6: giraDXPoco(); break;
            case 7: frena(); break;
            case 8: retromarcia(); break;
            default: decelera(); break;
        }
    }

    private void handleGearAndSafety(int gear, double trackPos) {
        action.gear = gear;

        // Safety override per posizioni estreme
        if (Math.abs(trackPos) > 0.9) {
            action.accelerate = 0.1;
            action.steering = (trackPos > 0) ? -0.5 : 0.5;
        }
    }

    // Azioni di guida con parametrizzazione dinamica
    private void accelera() {
        if (action.gear == -1) {
            action.gear = 1;
        }
        action.steering = 0;
        action.brake = 0;
        action.accelerate = 1;
    }

    private void giraSXMolto() {
        action.accelerate = 0;
        action.brake = 1;
        action.steering = 0.5;
    }

    private void giraSX() {
        action.accelerate = 0.5;
        action.brake = 0;
        action.steering = 0.25;
    }

    private void giraSXPoco() {
        action.accelerate = 1;
        action.brake = 0;
        action.steering = 0.1;
    }

    private void giraDXMolto() {
        action.accelerate = 0;
        action.brake = 1;
        action.steering = -0.5;
    }

    private void giraDX() {
        action.accelerate = 0.5;
        action.brake = 0;
        action.steering = -0.25;
    }

    private void giraDXPoco() {
        action.accelerate = 1;
        action.brake = 0;
        action.steering = -0.1;
    }

    private void frena() {
        action.steering = 0;
        action.accelerate = 0;
        action.brake = 1;
    }

    private void retromarcia() {
        double steerLock = 0.785398; // ad esempio, 45° in radianti
        action.gear = -1;
        action.brake = 0;
        action.accelerate = 0.3f;
        action.steering = (float) (-angle / steerLock);
    }

    private void decelera() {
        if (action.gear == -1) {
            action.gear = 1; // Uscita dalla retromarcia se in decelerazione
        }
        action.accelerate = 0;
        action.brake = 0.2f; // Piccola frenata per rallentare
        action.steering = 0;
    }
}
