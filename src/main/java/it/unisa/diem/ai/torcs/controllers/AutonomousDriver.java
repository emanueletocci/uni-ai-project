package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;
import it.unisa.diem.ai.torcs.utilities.RadarVisualizer;

import javax.swing.*;

public class AutonomousDriver extends BaseDriver {
    private static final RadarVisualizer radar = new RadarVisualizer();
    static {
        SwingUtilities.invokeLater(() -> RadarVisualizer.showRadar(radar));
    }
    private int cicliRecupero = 0;
    private final NearestNeighbor knn;
    private final Action action;

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
        double speed = sensors.getSpeed();
        double[] trackEdgeSensors = sensors.getTrackEdgeSensors();
        radar.updateSensors(trackEdgeSensors);

        // Recovery Policy
        if (Math.abs(angle) > stuckAngle) {
            stuck++;
        } else if (Math.abs(angle) < 0.2 && Math.abs(position) < 1.0 && speed > 5) {
            // Esci dalla recovery solo se l’auto è ragionevolmente allineata e sulla pista
            stuck = 0;
        }

        // Auto Bloccata
        if (stuck > stuckTime) {
            cicliRecupero++;
            float steer = (float) (-sensors.getAngleToTrackAxis() / steerLock);
            int gear = -1;
            if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
                gear = 1;
                steer = -steer;
            }
            clutch = clutching(sensors, clutch);
            action.gear = gear;
            action.steering = steer;
            action.accelerate = 0.5;
            action.brake = 0;
            action.clutch = clutch;
            System.out.println("Auto bloccata, ciclo: " + cicliRecupero);
        } else {
            // Estrazione e normalizzazione delle feature
            double[] features = FeatureNormalizer.extractAndNormalizeFeatures(
                    trackEdgeSensors,
                    position,
                    angle,
                    speed
            );

            // Classificazione tramite KNN
            int predictedClass = knn.classify(new Sample(features), 3);
            ClassLabel label = ClassLabel.fromCode(predictedClass);


            // Azione in base alla classe predetta (allineata al dataset semplificato)
            switch (label) {
                case ACCELERA_DRITTO:
                    acceleraDritto(action);
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
                default:
                    mantieniVelocita(action);
                    break;
            }

            // Cambio marcia automatico
            action.gear = getGear(sensors);
        }
        return action;
    }


    // Azioni di guida semplificate
    // Dritto (accelera)
    private void acceleraDritto(Action action) {
        action.steering = 0.0;
        action.brake = 0.0;
        action.accelerate = 1.0;
    }

    // Gira leggermente a sinistra
    private void giraLeggeroSinistra(Action action) {
        action.steering = 0.3f;
        action.brake = 0.0;
        action.accelerate = 0.8f;
    }

    // Gira forte a sinistra
    private void giraForteSinistra(Action action) {
        action.steering = 0.7f;
        action.brake = 0.0;
        action.accelerate = 0.6f;
    }

    // Gira leggermente a destra
    private void giraLeggeroDestra(Action action) {
        action.steering = -0.3f;
        action.brake = 0.0;
        action.accelerate = 0.8f;
    }

    // Gira forte a destra
    private void giraForteDestra(Action action) {
        action.steering = -0.7f;
        action.brake = 0.0;
        action.accelerate = 0.6f;
    }

    // Gira a sinistra (unico metodo)
    private void giraSinistra(Action action) {
        action.steering = 0.5f;
        action.brake = 0.0;
        action.accelerate = 0.7f;
    }

    // Gira a destra (unico metodo)
    private void giraDestra(Action action) {
        action.steering = -0.5f;
        action.brake = 0.0;
        action.accelerate = 0.7f;
    }

    // Frena (dritto)
    private void frena(Action action, SensorModel sensors) {
        action.steering = 0.0;
        action.accelerate = 0.0;
        action.brake = filterABS(sensors, 1.0f); // Freno con ABS
    }

    // Retromarcia (con correzione angolo)
    private void retromarcia(Action action, SensorModel sensors) {
        action.gear = -1;
        action.accelerate = 0.5f;
        action.brake = 0.0;
        // Correggi la direzione in base all'angolo rispetto all'asse della pista
        action.steering = (float) (-sensors.getAngleToTrackAxis() / (Math.PI / 2));
    }

    // Nessuna azione / decelerazione
    private void mantieniVelocita(Action action) {
        action.accelerate = 0.5f;
        action.brake = 0.0;
        action.steering = 0.0;
    }


    /*
    @Override
    public float[] initAngles() {
        float[] angles = new float[19];
        for (int i = 0; i < 5; i++) {
            angles[i] = -90 + i * 15;
            angles[18 - i] = 90 - i * 15;
        }
        for (int i = 5; i < 9; i++) {
            angles[i] = -20 + (i - 5) * 5;
            angles[18 - i] = 20 - (i - 5) * 5;
        }
        angles[9] = 0;
        return angles;
    }
    */

}
