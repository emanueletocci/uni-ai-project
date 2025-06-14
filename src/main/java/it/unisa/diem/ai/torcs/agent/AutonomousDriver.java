package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.*;
import it.unisa.diem.ai.torcs.utils.*;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;

/**
 * AutonomousDriver è un agente di guida autonoma per TORCS.
 * Utilizza due classificatori KNN distinti: uno per la guida normale,
 * e uno per la modalità di recupero da situazioni critiche (fuori pista o stallo).
 */
public class AutonomousDriver extends BaseDriver {

    private final FeatureExtractor extractor;
    private final FeatureNormalizer normalizer;
    private final NearestNeighbor driverKNN;
    private final NearestNeighbor recoveryKNN;
    private final Dataset recoveryDataset;
    private final Dataset driverDataset;

    Action action = new Action();

    /**
     * Costruttore: carica i dataset, li normalizza e inizializza i classificatori.
     */
    public AutonomousDriver() {
        normalizer = new FeatureNormalizer();
        extractor = new FeatureExtractor();

        driverDataset = Dataset.loadFromCSV("data/driver_dataset.csv");
        recoveryDataset = Dataset.loadFromCSV("data/recovery_dataset.csv");
        driverDataset.shuffle();
        recoveryDataset.shuffle();

        driverKNN = new NearestNeighbor(driverDataset);
        recoveryKNN = new NearestNeighbor(recoveryDataset);
    }

    /**
     * Metodo principale di controllo, chiamato ad ogni ciclo di simulazione.
     * Valuta se l'auto è bloccata; in tal caso attiva la recovery.
     * Altrimenti predice l'azione corretta tramite KNN.
     *
     * @param sensors Modello dei sensori del veicolo
     * @return un oggetto Action contenente i comandi da inviare a TORCS
     */
    @Override
    public Action control(SensorModel sensors) {
        double angle = sensors.getAngleToTrackAxis();

        if (Math.abs(angle) > stuckAngle) {
            stuck++;
        } else {
            stuck = 0; // Reset se non è bloccato
        }

        // Modalità recovery: auto considerata bloccata
        if (stuck > stuckAngle) {
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

        } else {
            // Guida normale: predizione tramite KNN
            FeatureVector rawFeatures = extractor.extractFeatures(sensors);
            FeatureVector normalizedFeatures = normalizer.normalize(rawFeatures);
            Sample testSample = new Sample(normalizedFeatures, null);

            int k = 1;
            int predictedClass = driverKNN.classify(testSample, k);
            Label predictedLabel = Label.fromCode(predictedClass);
            System.out.println("Predicted class: " + predictedLabel);

            // Conversione da label ad azione
            action.reset();
            switch (predictedLabel) {
                case GIRA_SINISTRA:
                    giraSinistra(action, sensors);
                    break;
                case ACCELERA:
                    accelera(action, sensors);
                    break;
                case GIRA_DESTRA:
                    giraDestra(action, sensors);
                    break;
                case FRENA:
                    frena(action, sensors);
                    break;
                case RETROMARCIA:
                    retromarcia(action, sensors);
                    break;
                default:
                    decelera(action, sensors);
                    break;
            }
        }

        return action;
    }

    /**
     * Metodo chiamato alla chiusura della simulazione.
     */
    @Override
    public void shutdown() {
        System.out.println("AutonomousDriver: Bye bye!");
    }

    /**
     * Metodo chiamato al reset della simulazione.
     */
    @Override
    public void reset() {
        System.out.println("AutonomousDriver: Restarting the race!");
    }
}
