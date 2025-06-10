package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.sensors.MessageBasedSensorModel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.utilities.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utilities.DataLogger;
import it.unisa.diem.ai.torcs.utilities.KeyInput;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer; // Import della normalizzazione
import javax.swing.*;

public class HumanDriver extends Controller {

    static {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }

    final int[] gearUp = {5000, 6000, 6000, 6500, 7000, 0};
    final int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};
    final float clutchMax = 0.5f;
    final float clutchDelta = 0.05f;
    final float clutchDec = 0.01f;
    private float clutch = 0;

    private final DataLogger lightLogger;

    public HumanDriver(){
        lightLogger = new DataLogger("data/dataset.csv");
    }

    @Override
    public Action control(SensorModel sensors) {
        Action action = new Action();
        MessageBasedSensorModel model = (MessageBasedSensorModel) sensors;

        // 1. Lettura sensori
        double[] track = model.getTrackEdgeSensors();
        double trackPos = model.getTrackPosition();
        double angle = model.getAngleToTrackAxis();
        double speedX = model.getSpeed();
        int gear = model.getGear();

        // 2. Gestione manuale: acceleratore, freno e retromarcia
        if (KeyInput.brake) {
            action.brake = 0.8f;
            action.accelerate = 0.0f;
        } else if (KeyInput.down && speedX < 1.0) {
            action.gear = -1;
            action.accelerate = 1.0f;
            action.brake = 0.0f;
        } else if (KeyInput.up) {
            action.accelerate = 1.0f;
            action.brake = 0.0f;
            if (gear < 1) action.gear = 1;
        } else {
            action.accelerate = 0.0f;
            action.brake = 0.0f;
        }

        // 3. Cambio automatico SOLO se non in retromarcia
        if (action.gear != -1) {
            action.gear = getGear(sensors);
            updateClutch(gear, action.gear);
        }

        // 4. Sterzata semplice e poco sensibile
        float steeringInput = 0.0f;
        if (KeyInput.left) steeringInput += 1.0f;
        if (KeyInput.right) steeringInput -= 1.0f;
        float steeringSensitivity = 0.3f;
        action.steering = Math.min(1.0f, steeringInput * steeringSensitivity);

        // 5. Calcolo classLabel (opzionale, per behavioral cloning)
        int classLabel = calculateClassLabel(action);

        // 6. Normalizzazione feature per logging

        double[] lightFeatures = FeatureNormalizer.extractAndNormalizeFeatures(track, trackPos, angle, speedX);
        lightLogger.log(lightFeatures, classLabel);

        return action;
    }

    private int calculateClassLabel(Action action) {
        final float SOGLIA_MOLTO = 0.7f;
        final float SOGLIA_POCO = 0.2f;

        if (action.accelerate > 0.8 && action.gear > 0 && Math.abs(action.steering) < SOGLIA_POCO && action.brake == 0) {
            return 0; // Accelera dritto
        }
        if (action.steering > SOGLIA_MOLTO) {
            return 1; // Gira molto a sinistra
        }
        if (action.steering > SOGLIA_POCO) {
            return 2; // Gira a sinistra
        }
        if (action.steering > 0) {
            return 3; // Gira poco a sinistra
        }
        if (action.steering < -SOGLIA_MOLTO) {
            return 4; // Gira molto a destra
        }
        if (action.steering < -SOGLIA_POCO) {
            return 5; // Gira a destra
        }
        if (action.steering < 0) {
            return 6; // Gira poco a destra
        }
        if (action.brake > 0.1) {
            return 7; // Frena
        }
        if (action.gear == -1 && action.accelerate > 0) {
            return 8; // Retromarcia
        }
        return 9; // Decelera o nessuna azione
    }

    private void updateClutch(int currentGear, int newGear) {
        if(currentGear != newGear) {
            clutch = Math.min(clutchMax, clutch + clutchDelta);
        } else {
            clutch = Math.max(0.0f, clutch - clutchDec);
        }
    }

    private int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        if (gear < 1)
            return 1;
        if (gear < 6 && rpm >= gearUp[gear - 1])
            return gear + 1;
        if (gear > 1 && rpm <= gearDown[gear - 1])
            return gear - 1;
        return gear;
    }

    @Override
    public void reset() {
        System.out.println("resetto");
    }

    @Override
    public void shutdown() {
        System.out.println("Registrazione completata su file CSV.");
    }
}
