package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.*;
import it.unisa.diem.ai.torcs.utils.*;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;

public class AutonomousDriver extends BaseDriver {
    private final FeatureExtractor extractor;
    private final FeatureNormalizer normalizer;
    private final NearestNeighbor knn;


    public AutonomousDriver() {
        // Carica dataset e normalizzatore
        Dataset trainingSet = Dataset.loadFromCSV("data/dataset_normalizzato.csv");
        normalizer = new FeatureNormalizer();
        extractor = new FeatureExtractor();
        knn = new NearestNeighbor(trainingSet);
    }

    @Override
    public Action control(SensorModel sensors) {
        double angle = sensors.getAngleToTrackAxis();

        if(Math.abs(angle) > stuckAngle) {
            stuck++;
        } else {
            stuck = 0; // Reset se non è bloccato
        }

        // AUTO BLOCCATA
        if(stuck > stuckAngle) {
            Action action = new Action();
            System.out.println("Auto bloccata per: " + stuck + " turni, correzione in corso...");
            float steer = (float) (-sensors.getAngleToTrackAxis() / steerLock);
            int gear = -1;
            if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
                gear = 1;
                steer = -steer;
            }
            action.gear = gear;
            action.steering = steer;
            action.accelerate = 1.0;
            action.brake = 0;
            action.clutch = clutching(sensors, clutchMax);

            return action;
        } else {
            // 1. Estrai le feature dal sensore
            FeatureVector rawFeatures = extractor.extractFeatures(sensors);

            // 2. Normalizza le feature
            FeatureVector normalizedFeatures = normalizer.normalize(rawFeatures);

            // 3. Crea un Sample "dummy" da classificare (label non serve)
            Sample testSample = new Sample(normalizedFeatures, null);

            // 4. Predici la label tramite il classificatore KNN
            // o il valore ottimale scelto

            int k = 10;
            int predictedClass = knn.classify(testSample, k);
            Label predictedLabel = Label.fromCode(predictedClass);
            System.out.println("Predicted class: " + predictedLabel);

            // 5. Mappa la label in un oggetto Action
            Action action = labelToAction(predictedLabel, sensors);

            if(sensors.getTrackPosition() > 1.0){
                System.out.println("Fuori pista a sinistra");
                giraDestra(action, sensors);
            } else if(sensors.getTrackPosition() < -1.0){
                System.out.println("Fuori pista a destra");
                giraSinistra(action, sensors);
            }

            return action;
        }
    }

    /**
     * Mappa una label predetta in un oggetto Action da inviare a TORCS.
     */
    private Action labelToAction(Label label, SensorModel sensors) {
        Action action = new Action();

        switch (label) {
            case AVANTI_SINISTRA:
                giraSinistra(action, sensors);
                break;
            case AVANTI_DRITTO:
                accelera(action, sensors);
                break;
            case AVANTI_DESTRA:
                giraDestra(action, sensors);
                break;
            case FRENA:
                frena(action, sensors);
                break;
            case RETROMARCIA:
                retromarcia(action, sensors);
                break;
        }
        return action;
    }

    @Override
    public void shutdown() {
        System.out.println("AutonomousDriver: Bye bye!");
    }

    @Override
    public void reset() {
        System.out.println("AutonomousDriver: Restarting the race!");
    }
}
