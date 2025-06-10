package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.sensors.MessageBasedSensorModel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.utilities.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utilities.DataLogger;
import it.unisa.diem.ai.torcs.utilities.KeyInput;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;
import javax.swing.*;

public class HumanDriver extends Controller {

    static {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }

    private final DataLogger logger;

    // Valori di sterzata intermedi (modifica se vuoi sterzate più o meno decise)
    private static final float STEER_NONE = 0.0f;
    private static final float STEER_SOFT_LEFT = 0.4f;
    private static final float STEER_SOFT_RIGHT = -0.4f;

    public HumanDriver() {
        logger = new DataLogger("data/dataset.csv");
    }

    @Override
    public Action control(SensorModel sensors) {
        Action action = new Action();
        MessageBasedSensorModel model = (MessageBasedSensorModel) sensors;

        // Lettura sensori principali
        double[] track = model.getTrackEdgeSensors();
        double trackPos = model.getTrackPosition();
        double angle = model.getAngleToTrackAxis();
        double speedX = model.getSpeed();
        int gear = model.getGear();

        // Gestione manuale: acceleratore (W), freno (Space), retromarcia (S), sterzo (A/D)
        if (KeyInput.brake) { // Space
            action.brake = 0.8f;
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
        int classLabel = calculateClassLabel(action);
        double[] features = FeatureNormalizer.extractAndNormalizeFeatures(track, trackPos, angle, speedX);
        logger.log(features, classLabel);

        return action;
    }

    /*
     * CLASSI
     * 0: Accelera dritto
     * 1: Accelera e gira a sinistra
     * 2: Accelera e gira a destra
     * 3: Frena dritto
     * 4: Retromarcia
     * 5: Nessuna azione/decelerazione
     * 6: Solo gira a sinistra (senza accelerare/frenare)
     * 7: Solo gira a destra (senza accelerare/frenare)
     */
    private int calculateClassLabel(Action action) {
        if (action.gear == -1 && action.accelerate > 0) return 4; // Retromarcia
        if (action.brake > 0.1) return 3; // Frena dritto

        // Accelerazione e sterzata
        if (action.accelerate > 0.5) {
            if (action.steering > 0.2) return 1; // Accelera e gira a sinistra
            if (action.steering < -0.2) return 2; // Accelera e gira a destra
            return 0; // Accelera dritto
        }

        // Solo sterzata senza accelerazione
        if (action.steering > 0.2) return 6; // Solo gira a sinistra
        if (action.steering < -0.2) return 7; // Solo gira a destra

        return 5; // Nessuna azione/decelerazione
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

    @Override
    public void reset() {
        System.out.println("Resetto");
    }

    @Override
    public void shutdown() {
        System.out.println("Registrazione completata su file CSV.");
    }
}
