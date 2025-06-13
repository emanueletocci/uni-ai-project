package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.*;
import it.unisa.diem.ai.torcs.utils.*;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;

public class AutonomousDriver extends Controller {
    private final FeatureExtractor extractor;
    private final FeatureNormalizer normalizer;
    private final NearestNeighbor knn;
    private final int K = 3; // o il valore ottimale scelto

    public AutonomousDriver() {
        // Carica dataset e normalizzatore
        Dataset trainingSet = Dataset.loadFromCSV("data/dataset_normalizzato.csv");
        normalizer = new FeatureNormalizer();
        extractor = new FeatureExtractor();
        knn = new NearestNeighbor(trainingSet);
    }

    @Override
    public Action control(SensorModel sensors) {
        // 1. Estrai le feature dal sensore
        FeatureVector rawFeatures = extractor.extractFeatures(sensors);

        // 2. Normalizza le feature
        FeatureVector normalizedFeatures = normalizer.normalize(rawFeatures);

        // 3. Crea un Sample "dummy" da classificare (label non serve)
        Sample testSample = new Sample(normalizedFeatures, null);

        // 4. Predici la label tramite il classificatore KNN
        int predictedClass = knn.classify(testSample, K);
        Label predictedLabel = Label.fromCode(predictedClass);

        // 5. Mappa la label in un oggetto Action
        Action action = labelToAction(predictedLabel, sensors);

        return action;
    }

    /**
     * Mappa una label predetta in un oggetto Action da inviare a TORCS.
     */
    private Action labelToAction(Label label, SensorModel sensors) {
        Action action = new Action();
        switch (label) {
            case AVANTI_SINISTRA:
                action.accelerate = 0.8;
                action.steering = -0.3f;
                action.gear = getGear(sensors);
                break;
            case AVANTI_DRITTO:
                action.accelerate = 1.0;
                action.steering = 0.0;
                action.gear = getGear(sensors);
                break;
            case AVANTI_DESTRA:
                action.accelerate = 0.8;
                action.steering = 0.3f;
                action.gear = getGear(sensors);
                break;
            case FRENA:
                action.brake = 1.0;
                action.gear = getGear(sensors);
                break;
            case RETROMARCIA:
                action.gear = -1;
                action.accelerate = 1.0;
                break;
        }
        // Puoi aggiungere logica per frizione, ABS, ecc.
        return action;
    }

    private int getGear(SensorModel sensors) {
        // Puoi copiare la logica dal tuo HumanDriver o SimpleDriver
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();
        final int[] gearUp = {5000, 6000, 6000, 6500, 7000, 0};
        final int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};

        if (gear < 1) return 1;
        if (gear < 6 && rpm >= gearUp[gear - 1]) return gear + 1;
        if (gear > 1 && rpm <= gearDown[gear - 1]) return gear - 1;
        return gear;
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
