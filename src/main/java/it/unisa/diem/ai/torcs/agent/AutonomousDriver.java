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
        boolean isRecovery = stuck > stuckAngle
                || Math.abs(sensors.getAngleToTrackAxis()) > 0.5
                || Math.abs(sensors.getTrackPosition()) > 0.9
                || Math.abs(sensors.getLateralSpeed()) > 15;

        // Estrai feature e normalizzale
        FeatureVector rawFeatures = extractor.extractFeatures(sensors);
        FeatureVector normalizedFeatures = normalizer.normalize(rawFeatures);
        Sample testSample = new Sample(normalizedFeatures, null);

        // Modalità recovery: auto considerata bloccata
        if (isRecovery) {
            int k = 1;
            int predictedClass = recoveryKNN.classify(testSample, k);
            Label predictedLabel = Label.fromCode(predictedClass);

            System.out.println("🛟 [RECOVERY] Predicted: " + predictedLabel);

            // Applica l’azione in base alla label
            action.reset();
            switch (predictedLabel) {
                case GIRA_SINISTRA -> giraSinistra(action, sensors);
                case ACCELERA      -> accelera(action, sensors);
                case GIRA_DESTRA   -> giraDestra(action, sensors);
                case FRENA         -> frena(action, sensors);
                case RETROMARCIA   -> retromarcia(action, sensors);
                default            -> decelera(action, sensors);
            }

        } else {
            // Guida normale: predizione tramite KNN
            int k = 1;
            int predictedClass = driverKNN.classify(testSample, k);
            Label predictedLabel = Label.fromCode(predictedClass);
            System.out.println("\uD83D\uDFE2 [NORMAL] Predicted: " + predictedLabel);

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
