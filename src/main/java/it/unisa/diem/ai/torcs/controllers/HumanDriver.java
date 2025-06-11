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

         if (sensors.getSpeed() > 1 || action.gear != -1d)
            action.gear = getGear(sensors);

        if (sensors.getSpeed() < 1 || action.gear == -1)
            action.accelerate = 1d;
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
            0 = acceleraDritto
            1 = giraLeggeroSinistra
            2 = giraForteSinistra
            3 = giraLeggeroDestra
            4 = giraForteDestra
            5 = frena
            6 = retromarcia
            7 = mantieniVelocita
         */

    private int calculateClassLabel(Action action) {
        if (action.gear == -1 && action.accelerate > 0) return 6; // Retromarcia
        if (action.brake > 0.1) return 5; // Frena
        if (action.steering >= 0.6f) return 2; // Gira forte a sinistra
        if (action.steering >= 0.15f) return 1; // Gira leggermente a sinistra
        if (action.steering <= -0.6f) return 4; // Gira forte a destra
        if (action.steering <= -0.15f) return 3; // Gira leggermente a destra
        if (action.accelerate > 0.7f) return 0; // Accelera dritto
        return 7; // Nessuna azione / decelerazione
    }


    // Cambio marcia automatico come da specifica TORCS
    
    

    @Override
    public void reset() {
        System.out.println("Resetto");
    }

    @Override
    public void shutdown() {
        System.out.println("Registrazione completata su file CSV.");
    }
}
