package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;

public class AutonomousDriver extends Controller {
    final int[] gearUp = {5000, 6000, 6000, 6500, 7000, 0};
    final int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};

    final int stuckTime = 25;
    final float stuckAngle = (float) 0.523598775; // PI/6

    final float maxSpeedDist = 70;
    final float maxSpeed = 150;
    final float sin5 = (float) 0.08716;
    final float cos5 = (float) 0.99619;

    final float steerLock = (float) 0.785398;
    final float steerSensitivityOffset = (float) 80.0;
    final float wheelSensitivityCoeff = 1;

    final float[] wheelRadius = {(float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276};
    final float absSlip = (float) 2.0;
    final float absRange = (float) 3.0;
    final float absMinSpeed = (float) 3.0;

    final float clutchMax = (float) 0.5;
    final float clutchDelta = (float) 0.05;
    final float clutchRange = (float) 0.82;
    final float clutchDeltaTime = (float) 0.02;
    final float clutchDeltaRaced = 10;
    final float clutchDec = (float) 0.01;
    final float clutchMaxModifier = (float) 1.3;
    final float clutchMaxTime = (float) 1.5;

    private int stuck = 0;
    private float clutch = 0;
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
            action.accelerate = 1.0;
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

        /*
         * CLASSI
            0 = acceleraDritto
            1 = giraLeggeroSinistra
            2 = giraForteSinistra
            3 = giraLeggeroDestra
            4 = giraForteDestra
            5 = frena
            6 = retromarcia
            7 = mantieniVelocita
         */

            // Azione in base alla classe predetta (allineata al dataset semplificato)
            switch (label) {
                case ACCELERA_DRITTO:
                    acceleraDritto(action);
                    break;
                case GIRA_LEGGERO_SINISTRA:
                    giraLeggeroSinistra(action);
                    break;
                case GIRA_FORTE_SINISTRA:
                    giraForteSinistra(action);
                    break;
                case GIRA_LEGGERO_DESTRA:
                    giraLeggeroDestra(action);
                    break;
                case GIRA_FORTE_DESTRA:
                    giraForteDestra(action);
                    break;
                case FRENA:
                    frena(action);
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

    // Cambio marcia automatico
    private int getGear(SensorModel sensors) {
        final int[] gearUp = {5000, 6000, 6000, 6500, 7000, 0};
        final int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        if (gear < 1) return 1;
        if (gear < 6 && rpm >= gearUp[gear - 1]) return gear + 1;
        if (gear > 1 && rpm <= gearDown[gear - 1]) return gear - 1;
        return gear;
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

    // Frena (dritto)
    private void frena(Action action) {
        action.steering = 0.0;
        action.accelerate = 0.0;
        action.brake = 1.0;
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


    @Override
    public void reset() {
        System.out.println("Restarting the race!");
    }

    @Override
    public void shutdown() {
        System.out.println("Bye bye!");
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

    float clutching(SensorModel sensors, float clutch) {
        float maxClutch = clutchMax;
        if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE
                && sensors.getDistanceRaced() < clutchDeltaRaced)
            clutch = maxClutch;

        if (clutch > 0) {
            double delta = clutchDelta;
            if (sensors.getGear() < 2) {
                delta /= 2;
                maxClutch *= clutchMaxModifier;
                if (sensors.getCurrentLapTime() < clutchMaxTime)
                    clutch = maxClutch;
            }
            clutch = Math.min(maxClutch, clutch);
            if (clutch != maxClutch) {
                clutch -= (float) delta;
                clutch = Math.max((float) 0.0, clutch);
            } else
                clutch -= clutchDec;
        }
        return clutch;
    }
}
