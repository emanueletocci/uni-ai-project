package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.*;
import it.unisa.diem.ai.torcs.utils.*;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;

public class AutonomousDriver extends Controller {
    private final FeatureExtractor extractor;
    private final FeatureNormalizer normalizer;
    private final NearestNeighbor knn;
    private final int K = 10; // o il valore ottimale scelto

    private static final int STUCK_TIME = 25;
    private static final float STUCK_ANGLE = (float) 0.523598775; // PI/6

    /* Costanti di cambio marcia */
    private static final int[] GEAR_UP = {5000, 6000, 6000, 6500, 7000, 0};
    private static final int[] GEAR_DOWN = {0, 2500, 3000, 3000, 3500, 3500};

    /* Costanti del filtro ABS */
    private static final float[] WHEEL_RADIUS = {(float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276};
    private static final float ABS_SLIP = 2.0f;
    private static final float ABS_RANGE = 3.0f;
    private static final float ABS_MIN_SPEED = 3.0f;

    /* Costanti di frizione */
    private static final float CLUTCH_MAX = 0.5f;
    private static final float CLUTCH_DELTA = 0.05f;
    private static final float CLUTCH_RANGE = 0.82f;
    private static final float CLUTCH_DELTA_TIME = 0.02f;
    private static final float CLUTCH_DELTA_RACED = 10;
    private static final float CLUTCH_DEC = 0.01f;
    private static final float CLUTCH_MAX_MODIFIER = 1.3f;
    private static final float CLUTCH_MAX_TIME = 1.5f;

    /* Costanti di sterzata */
    private static final float STEER_LOCK = (float) Math.PI/4; // 0.785398
    private static final float STEER_SENSITIVITY_OFFSET = 80.0f;
    private static final float WHEEL_SENSITIVITY = 1.0f;

    private int stuck = 0;

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

        if(Math.abs(angle) > STUCK_ANGLE) {
            stuck++;
        } else {
            stuck = 0; // Reset se non è bloccato
        }

        // AUTO BLOCCATA
        if(stuck > STUCK_TIME) {
            Action action = new Action();
            System.out.println("Auto bloccata per: " + stuck + " turni, correzione in corso...");
            float steer = (float) (-sensors.getAngleToTrackAxis() / STEER_LOCK);
            int gear = -1;
            if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
                gear = 1;
                steer = -steer;
            }
            action.gear = gear;
            action.steering = steer;
            action.accelerate = 1.0;
            action.brake = 0;
            action.clutch = clutching(sensors, CLUTCH_MAX);

            return action;
        } else {
            // 1. Estrai le feature dal sensore
            FeatureVector rawFeatures = extractor.extractFeatures(sensors);

            // 2. Normalizza le feature
            FeatureVector normalizedFeatures = normalizer.normalize(rawFeatures);

            // 3. Crea un Sample "dummy" da classificare (label non serve)
            Sample testSample = new Sample(normalizedFeatures, null);

            // 4. Predici la label tramite il classificatore KNN
            int predictedClass = knn.classify(testSample, K);
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

    private void accelera(Action action, SensorModel sensors) {
        action.accelerate = 1.0;
        action.brake = 0.0;
        action.steering = 0f;
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
    }

    private void frena(Action action, SensorModel sensors) {
        action.brake = filterABS(sensors, 1f);
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
        action.accelerate = 0.0f;
    }

    private void retromarcia(Action action, SensorModel sensors) {
        action.gear = -1;
        action.accelerate = 1.0f;
        action.brake = 0.0;
        action.clutch = CLUTCH_MAX;
    }

    private void giraSinistra(Action action, SensorModel sensors) {
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
        action.steering = 0.3f;
    }

    private void giraDestra(Action action, SensorModel sensors) {
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
        action.steering = -0.3f;
    }

    private int getGear(SensorModel sensors) {
        // Puoi copiare la logica dal tuo HumanDriver o SimpleDriver
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        if (gear < 1) return 1;
        if (gear < 6 && rpm >= GEAR_UP[gear - 1]) return gear + 1;
        if (gear > 1 && rpm <= GEAR_DOWN[gear - 1]) return gear - 1;
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

    // Metodo ABS (identico all'originale)
    private float filterABS(SensorModel sensors, float brake) {
        float speed = (float) (sensors.getSpeed() / 3.6);
        if (speed < ABS_MIN_SPEED) return brake;

        float slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += sensors.getWheelSpinVelocity()[i] * WHEEL_RADIUS[i];
        }
        slip = speed - slip/4.0f;

        if (slip > ABS_SLIP) {
            brake = brake - (slip - ABS_SLIP)/ABS_RANGE;
        }
        return Math.max(brake, 0);
    }

    // Metodo per la gestione della frizione (identico all'originale)
    private float clutching(SensorModel sensors, float clutch) {
        float maxClutch = CLUTCH_MAX;

        if (sensors.getCurrentLapTime() < CLUTCH_DELTA_TIME &&
                getStage() == Stage.RACE &&
                sensors.getDistanceRaced() < CLUTCH_DELTA_RACED) {
            clutch = maxClutch;
        }

        if (clutch > 0) {
            float delta = CLUTCH_DELTA;
            if (sensors.getGear() < 2) {
                delta /= 2;
                maxClutch *= CLUTCH_MAX_MODIFIER;
                if (sensors.getCurrentLapTime() < CLUTCH_MAX_TIME) clutch = maxClutch;
            }

            clutch = Math.min(maxClutch, clutch);
            clutch = (clutch != maxClutch) ? Math.max(clutch - delta, 0) : clutch - CLUTCH_DEC;
        }
        return clutch;
    }


}
