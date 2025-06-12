package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.model.ClassLabel;
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

        // Lettura sensori principali
        // Estraggo tutti i e 19 i sensori di bordo pista
        double[] track = sensors.getTrackEdgeSensors();
        double trackPos = sensors.getTrackPosition();
        double angle = sensors.getAngleToTrackAxis();
        double speedX = sensors.getSpeed();
        double speedY = sensors.getLateralSpeed();
        int gear = sensors.getGear();

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

        if (isOffTrack(trackPos)) {
            System.out.println("🚨 ATTENZIONE: Auto fuori pista!");
        }

        return action;
    }

    /**
     * Restituisce true se l'auto è fuori pista, ovvero se il valore assoluto della posizione
     * rispetto al centro della pista è maggiore di 1.0.
     *
     * @param trackPos posizione dell'auto rispetto al centro pista (in [-inf, +inf])
     * @return true se l'auto è fuori pista
     */
    private boolean isOffTrack(double trackPos) {
        return Math.abs(trackPos) > 1.0;
    }

}
