package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;

public class AutonomousDriver extends BaseDriver {
    private int cicliRecupero = 0;
    private final NearestNeighbor knn;
    private final Action action;

    // Valori di sterzata intermedi (modifica se vuoi sterzate più o meno decise)
    private static final float STEER_SOFT_LEFT = 0.5f;
    private static final float STEER_SOFT_RIGHT = -0.5f;

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

        // Recovery Policy
        if (Math.abs(angle) > stuckAngle) {
            stuck++;
        } else if (Math.abs(angle) < 0.2 && Math.abs(position) < 1.0 && speedX > 5) {
            // Esci dalla recovery solo se l’auto è ragionevolmente allineata e sulla pista
            stuck = 0;
        }

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

        return action;
    }


    // Azioni di guida semplificate
    // Dritto (accelera)
    private void acceleraDritto(Action action) {
        action.steering = 0.0;
        action.brake = 0.0;
        action.accelerate = 1d;
    }


    // Gira a sinistra (unico metodo)
    private void giraSinistra(Action action) {
        action.steering = STEER_SOFT_LEFT;
        action.brake = 0.0;
        action.accelerate = 0.25d;
    }

    // Gira a destra (unico metodo)
    private void giraDestra(Action action) {
        action.steering = STEER_SOFT_RIGHT;
        action.brake = 0.0;
        action.accelerate = 0.25d;
    }

    // Frena (dritto)
    private void frena(Action action, SensorModel sensors) {
        action.steering = 0.0;
        action.accelerate = 0.0;
        action.brake = filterABS(sensors, 0.7f); // Freno con ABS
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
        action.accelerate = 0.3f;
        action.brake = 0.0;
        action.steering = 0.0;
    }


}
