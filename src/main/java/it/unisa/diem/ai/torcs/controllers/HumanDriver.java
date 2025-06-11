package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.sensors.MessageBasedSensorModel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.utilities.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utilities.DataLogger;
import it.unisa.diem.ai.torcs.utilities.KeyInput;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;
import javax.swing.*;

public class HumanDriver extends Controller {
    final int[] gearUp = { 5000, 6000, 6000, 6500, 7000, 0 };
    final int[] gearDown = { 0, 2500, 3000, 3000, 3500, 3500 };

    final int stuckTime = 25;
    final float stuckAngle = (float) 0.523598775; // PI/6

    final float maxSpeedDist = 70;
    final float maxSpeed = 150;
    final float sin5 = (float) 0.08716;
    final float cos5 = (float) 0.99619;

    final float steerLock = (float) 0.785398;
    final float steerSensitivityOffset = (float) 80.0;
    final float wheelSensitivityCoeff = 1;

    final float[] wheelRadius = { (float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276 };
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

    static {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }

    private final DataLogger logger;

    // Valori di sterzata intermedi (modifica se vuoi sterzate più o meno decise)
    private static final float STEER_NONE = 0.0f;
    private static final float STEER_SOFT_LEFT = 0.5f;
    private static final float STEER_SOFT_RIGHT = -0.5f;

    public HumanDriver() {
        super();
        logger = new DataLogger("data/dataset.csv");
    }

    @Override
    public Action control(SensorModel sensors) {
        Action action = new Action();
        MessageBasedSensorModel model = (MessageBasedSensorModel) sensors;

        // Lettura sensori principali
        // Estraggo tutti i e 19 i sensori di bordo pista
        double[] track = model.getTrackEdgeSensors();
        double trackPos = model.getTrackPosition();
        double angle = model.getAngleToTrackAxis();
        double speedX = model.getSpeed();
        int gear = model.getGear();

        // Gestione manuale: acceleratore (W), freno (Space), retromarcia (S), sterzo (A/D)
        if (KeyInput.brake) { // Space
            action.brake = filterABS(sensors, 0.8f);
            action.accelerate = 0.0f;
        } else if (KeyInput.down && speedX < 1.0) { // S + quasi fermo
            action.gear = -1;
            action.accelerate = 1.0f;
            action.brake = 0.0f;
        } else if (KeyInput.up) { // W
            action.accelerate = 1.0f;
            action.brake = 0.0f;
            if (gear < 1) action.gear = 1;
        } else {
            action.accelerate = 0.0f;
            action.brake = 0.0f;
        }

        // Cambio automatico SOLO se non in retromarcia
        if (action.gear != -1) {
            action.gear = getGear(sensors);
        }

        // Sterzata morbida: A = sinistra, D = destra, nessun accumulo
        if (KeyInput.left && !KeyInput.right) {
            action.steering = STEER_SOFT_LEFT;
        } else if (KeyInput.right && !KeyInput.left) {
            action.steering = STEER_SOFT_RIGHT;
        } else {
            action.steering = STEER_NONE;
        }

        // Logging dati normalizzati e class label (per behavioral cloning)
        int classLabel = ClassLabel.calculateLabel(action).getCode();
        double[] features = FeatureNormalizer.extractAndNormalizeFeatures(track, trackPos, angle, speedX);
        logger.log(features, classLabel);

        return action;
    }

    // Cambio marcia automatico come da specifica TORCS
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

    /*
     * DISPOSIZIONE DEI SENSORI DI BORDO PISTA (track edge sensors):
     *
     * Ogni sensore misura la distanza tra il veicolo e il bordo della pista in una specifica direzione,
     * espressa come angolo (in gradi) rispetto all’asse longitudinale dell’auto.
     * La seguente disposizione (custom) concentra più sensori vicino all’asse centrale e agli estremi,
     * aumentando la risoluzione dove la percezione è più critica per la guida autonoma.
     *
     * Indice   Angolo (gradi)
     *   0        -90
     *   1        -75
     *   2        -60
     *   3        -45
     *   4        -30
     *   5        -20
     *   6        -15
     *   7        -10
     *   8         -5
     *   9          0   (davanti all’auto)
     *  10         +5
     *  11        +10
     *  12        +15
     *  13        +20
     *  14        +30
     *  15        +45
     *  16        +60
     *  17        +75
     *  18        +90
     *
     * Nota: questa configurazione NON è la standard di TORCS (che prevede 19 sensori da -90° a +90° con passo 10°),
     * ma è una scelta personalizzata che permette maggiore precisione nelle zone più rilevanti per la traiettoria.
     */

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
    @Override
    public void reset() {
        System.out.println("Resetto");
    }

    @Override
    public void shutdown() {
        System.out.println("Registrazione completata su file CSV.");
    }

    private float filterABS(SensorModel sensors, float brake) {
        float speed = (float) (sensors.getSpeed() / 3.6);
        if (speed < absMinSpeed)
            return brake;
        float slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += (float) (sensors.getWheelSpinVelocity()[i] * wheelRadius[i]);
        }
        slip = speed - slip / 4.0f;
        if (slip > absSlip) {
            brake = brake - (slip - absSlip) / absRange;
        }
        if (brake < 0)
            return 0;
        else
            return brake;
    }
}
