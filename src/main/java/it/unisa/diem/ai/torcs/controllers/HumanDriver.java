package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.Action;
import it.unisa.diem.ai.torcs.utilities.*;

import javax.swing.*;

public class HumanDriver extends BaseDriver {
    private static final RadarVisualizer radar = new RadarVisualizer();
    int giriFuoriPista = 0;
    static {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
        SwingUtilities.invokeLater(() -> RadarVisualizer.showRadar(radar));
    }

    private final DataLogger logger;

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
        radar.updateSensors(track);
        double trackPos = sensors.getTrackPosition();
        double angle = sensors.getAngleToTrackAxis();
        double speedX = sensors.getSpeed();
        double speedY = sensors.getLateralSpeed();

        // --- GESTIONE COMBINATA: VELOCITÀ + STERZO ---

        if (KeyInput.brake) {
            frena(action, sensors);
        } else if (KeyInput.down && speedX < 5.0) {
            retromarcia(action, sensors);
        } else if (KeyInput.up) {
            accelera(action, sensors);
        }

        // Sovrascrive solo lo sterzo, mantenendo brake/accelerate impostati prima
        if (KeyInput.left && !KeyInput.right) {
            giraSinistra(action);
        } else if (KeyInput.right && !KeyInput.left) {
            giraDestra(action);
        }

        // Cambio automatico solo se non in retro
        if (action.gear != -1) {
            action.gear = getGear(sensors);
        }

        // Logging dati normalizzati e class label (per behavioral cloning)
        int classLabel = ClassLabel.calculateLabel(action).getCode();
        double[] features = FeatureNormalizer.extractAndNormalizeFeatures(track, trackPos, angle, speedX, speedY);
        logger.log(features, classLabel);

        if (isOffTrack(trackPos)) {
            giriFuoriPista++;
            System.out.println("🚨 ATTENZIONE: Auto fuori pista! Giro: : " + giriFuoriPista);
        }

        return action;
    }
}
