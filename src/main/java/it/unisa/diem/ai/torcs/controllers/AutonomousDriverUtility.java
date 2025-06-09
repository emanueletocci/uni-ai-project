package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.sensors.SensorModel;

public class AutonomousDriverUtility {

    private final NearestNeighbor knn;
    private final Action action;

    public AutonomousDriverUtility(String datasetPath) {
        knn = new NearestNeighbor(datasetPath);
        action = new Action();
    }

    public Action decide(SensorModel sensors, int gear) {
        // Feature del dataset light: 9 track selezionati + trackPos, angle, speedX
        double[] features = new double[12];

        // Sensori track selezionati (indici 0,3,4,8,9,10,14,15,18)
        double[] track = sensors.getTrackEdgeSensors();
        features[0] = track[0];   // track0
        features[1] = track[3];   // track3
        features[2] = track[4];   // track4
        features[3] = track[8];   // track8
        features[4] = track[9];   // track9
        features[5] = track[10];  // track10
        features[6] = track[14];  // track14
        features[7] = track[15];  // track15
        features[8] = track[18];  // track18

        // Aggiungi le altre features
        features[9] = sensors.getTrackPosition();   // trackPos
        features[10] = sensors.getAngleToTrackAxis(); // angle
        features[11] = sensors.getSpeed();          // speedX

        Sample testSample = new Sample(features);
        int predictedClass = knn.classify(testSample, 5); // k=5

        switch (predictedClass) {
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

        action.gear = gear;
        return action;
    }
    // 0: Accelera dritto
    private void accelera() {
        action.accelerate = 1.0;
        action.brake = 0.0;
        action.steering = 0.0;
        if (action.gear == -1) action.gear = 1; // Torna in marcia avanti se eri in retro
    }

    // 1: Gira molto a sinistra
    private void giraSXMolto() {
        action.accelerate = 0.7;   // accelera meno per non perdere aderenza
        action.brake = 0.0;
        action.steering = 1.0;     // sterzo massimo a sinistra
    }

    // 2: Gira a sinistra
    private void giraSX() {
        action.accelerate = 0.8;
        action.brake = 0.0;
        action.steering = 0.5;     // sterzo medio a sinistra
    }

    // 3: Gira poco a sinistra
    private void giraSXPoco() {
        action.accelerate = 1.0;
        action.brake = 0.0;
        action.steering = 0.2;     // sterzo leggero a sinistra
    }

    // 4: Gira molto a destra
    private void giraDXMolto() {
        action.accelerate = 0.7;
        action.brake = 0.0;
        action.steering = -1.0;    // sterzo massimo a destra
    }

    // 5: Gira a destra
    private void giraDX() {
        action.accelerate = 0.8;
        action.brake = 0.0;
        action.steering = -0.5;    // sterzo medio a destra
    }

    // 6: Gira poco a destra
    private void giraDXPoco() {
        action.accelerate = 1.0;
        action.brake = 0.0;
        action.steering = -0.2;    // sterzo leggero a destra
    }

    // 7: Frena
    private void frena() {
        action.accelerate = 0.0;
        action.brake = 1.0;        // freno massimo
        action.steering = 0.0;
    }

    // 8: Retromarcia
    private void retromarcia() {
        action.gear = -1;
        action.accelerate = 0.3;   // accelerazione moderata in retro
        action.brake = 0.0;
        action.steering = 0.0;
    }

    // default: Decelera (nessuna azione)
    private void decelera() {
        if (action.gear == -1) action.gear = 1;
        action.accelerate = 0.0;
        action.brake = 0.0;
        action.steering = 0.0;
    }

}
