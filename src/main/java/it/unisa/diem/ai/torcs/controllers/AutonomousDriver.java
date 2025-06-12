package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;
import it.unisa.diem.ai.torcs.utilities.RadarVisualizer;

import javax.swing.*;

public class AutonomousDriver extends BaseDriver {
    private final NearestNeighbor knn;
    private final Action action;

    private static final RadarVisualizer radar = new RadarVisualizer();

    static {
        SwingUtilities.invokeLater(() -> RadarVisualizer.showRadar(radar));
    }

    public AutonomousDriver() {
        // Percorso del dataset da behavioral cloning
        super();
        knn = new NearestNeighbor("data/dataset.csv");
        action = new Action();
    }

    @Override
    public Action control(SensorModel sensors) {
        // Lettura sensori principali
        double angle = sensors.getAngleToTrackAxis();
        double position = sensors.getTrackPosition();
        double speedX = sensors.getSpeed();
        double speedY = sensors.getLateralSpeed();
        double[] trackEdgeSensors = sensors.getTrackEdgeSensors();
        radar.updateSensors(trackEdgeSensors);

            // Estrazione e normalizzazione delle feature
            double[] features = FeatureNormalizer.extractAndNormalizeFeatures(
                    trackEdgeSensors,
                    position,
                    angle,
                    speedX,
                    speedY
            );

            // Classificazione tramite KNN
            int predictedClass = knn.classify(new Sample(features), 3);
            ClassLabel label = ClassLabel.fromCode(predictedClass);

            // Azione in base alla classe predetta (allineata al dataset semplificato)
            switch (label) {
                case ACCELERA:
                    accelera(action, sensors);
                    break;
                case GIRA_SINISTRA:
                    giraSinistra(action);
                    break;
                case GIRA_DESTRA:
                    giraDestra(action);
                    break;
                case FRENA:
                    frena(action, sensors);
                    break;
                case RETROMARCIA:
                    retromarcia(action, sensors);
                    break;
                case MANTIENI_VELOCITA:
                    mantieniVelocita(action);
                    break;
                default:
                    System.out.println("ERROR: unknown label " + label);
                    break;
            }

            // Cambio marcia automatico
            action.gear = getGear(sensors);

        if (isOffTrack(position)) {
            System.out.println("🚨 ATTENZIONE: Auto fuori pista!");
        }

        return action;
    }

}
