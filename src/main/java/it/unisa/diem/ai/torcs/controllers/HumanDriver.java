package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.sensors.MessageBasedSensorModel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.Action;
import it.unisa.diem.ai.torcs.utilities.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utilities.DataLogger;
import it.unisa.diem.ai.torcs.utilities.KeyInput;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;
import javax.swing.*;

public class HumanDriver extends BaseDriver {
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
        double speedY = model.getLateralSpeed();
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
        double[] features = FeatureNormalizer.extractAndNormalizeFeatures(track, trackPos, angle, speedX, speedY);
        logger.log(features, classLabel);

        return action;
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
}
