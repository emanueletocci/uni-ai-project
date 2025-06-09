package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.sensors.SensorModel;

public class AutonomousDriverUtility {
    //pilota autonomo che, dato lo stato dell'auto (sensori), decide quale azione intraprendere usando un classificatore k-NN
    private NearestNeighbor knn;

//inizializzo il classificatore leggendo un dataset che contiene coppie osservazione-azione
    public AutonomousDriverUtility(String datasetPath) {
        knn = new NearestNeighbor(datasetPath);
    }

    public Action decide(SensorModel sensors, int gear) {
        //tutti i 19 sensori (utility)
         double[] edge = sensors.getTrackEdgeSensors(); 
        // 1. Estrai le feature dai sensori (come nel dataset)
        double[] features = {
                sensors.getSpeed(),
                sensors.getTrackPosition(),
                sensors.getTrackEdgeSensors()[4],
                sensors.getTrackEdgeSensors()[6],
                sensors.getTrackEdgeSensors()[8],
                sensors.getTrackEdgeSensors()[9],
                sensors.getTrackEdgeSensors()[10],
                sensors.getTrackEdgeSensors()[12],
                sensors.getTrackEdgeSensors()[14],
                sensors.getAngleToTrackAxis()
        };        Sample testSample = new Sample(features);

        // Predici la classe attraverso il knn
        int predictedClass = knn.classify(new Sample(features), 5);

        // Mappa la classe su Action
        Action action = new Action();
        switch (predictedClass) {
            case 0: // Accelerazione
                action.accelerate = 1d;
                action.steering = 0d;
                action.brake = 0d;
                break;
            case 1: // Frenata
                action.brake = 0.5d;
                action.accelerate = 0d;
                action.steering = 0d;
                break;
            case 2: // Sterzata a sinistra
                action.steering = 0.5d;
                action.accelerate = 0.25d;
                action.brake = 0d;
                break;
            case 3: // Sterzata a destra
                action.steering = -0.5d;
                action.accelerate = 0.25d;
                action.brake = 0d;
                break;
            case 4: // Retromarcia
                action.gear = -1;
                action.accelerate = 0.6d;
                action.steering = 0d;
                action.brake = 0d;
                break;
            case 5: // Default
                action.accelerate = 0.3d;
                action.steering = 0d;
                action.brake = 0d;
                break;
        }
        action.gear = gear;
        return action;
    }
}


