package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.*;
import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.utils.FeatureExtractor;
import it.unisa.diem.ai.torcs.model.KeyInput;

import it.unisa.diem.ai.torcs.io.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utils.FeatureNormalizer;
import it.unisa.diem.ai.torcs.utils.RadarVisualizer;

import javax.swing.*;

public class HumanDriver extends BaseDriver {
    private static final RadarVisualizer radar = new RadarVisualizer();
    static {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
        SwingUtilities.invokeLater(() -> RadarVisualizer.showRadar(radar));
    }
    private final Dataset rawDataset; // Dataset grezzo
    private final Dataset datasetNormalizzato;    // Dataset normalizzato
    private final Dataset recoveryDataset;
    private final Dataset driverDataset;
    private final FeatureExtractor extractor;
    private final FeatureNormalizer normalizer;

    public HumanDriver() {
        rawDataset = new Dataset();             // Dataset completo in formato grezzo
        datasetNormalizzato = new Dataset();    // Dataset completo in formato normalizzato
        recoveryDataset = new Dataset();        // Dataset per le situazioni di recupero, normalizzato
        driverDataset = new Dataset();          // Dataset per le situazioni di guida normale, normalizzato
        extractor = new FeatureExtractor();
        normalizer = new FeatureNormalizer();

    }

    @Override
    public Action control(SensorModel sensors) {
        radar.updateSensors(sensors.getTrackEdgeSensors());

        // 1. Leggi i comandi dalla tastiera (KeyInput)
        Action action = new Action();
        double speedX = sensors.getSpeed();

        // Gestione accelerazione, frenata, retromarcia
        if (KeyInput.brake) {
            frena(action, sensors);
        } else if (KeyInput.down && speedX < 5.0) {
            retromarcia(action, sensors);
        } else if (KeyInput.up) {
            accelera(action, sensors);
        } else {
            // Nessun comando di accelerazione o frenata
            action.accelerate = 0.0;
            action.brake = 0.0;
        }

        // Gestione sterzata (sovrascrive solo lo sterzo)
        if (KeyInput.left && !KeyInput.right) {
            giraSinistra(action, sensors);
        } else if (KeyInput.right && !KeyInput.left) {
            giraDestra(action, sensors);
        } else {
            // Nessuna sterzata o sterzo centrato
            action.steering = 0.0;
        }

        // Cambio marcia automatico solo se non in retromarcia
        if (action.gear != -1) {
            action.gear = getGear(sensors);
        }

        // Gestione clutch e ABS (opzionale, puoi riutilizzare i tuoi metodi)
        action.clutch = clutching(sensors, (float) action.clutch);
        if (action.brake > 0) {
            action.brake = filterABS(sensors, (float) action.brake);
        }

        // Discretizza l'azione in una label
        Label label = Label.fromAction(action);

        // Estrai le feature dai sensori
        FeatureVector rawFeatures = extractor.extractFeatures(sensors);
        FeatureVector featuresNormalizzate = normalizer.normalize(rawFeatures);

        // Criterio di filtraggio: solo guida in pista
        double trackPos = sensors.getTrackPosition();
        double angle = sensors.getAngleToTrackAxis();
        double speedY = sensors.getLateralSpeed();
        boolean isDriving = Math.abs(trackPos) <= 0.9 && Math.abs(angle) <= 0.5 && Math.abs(speedY) <= 15;

        Sample rawSample = new Sample(rawFeatures, label);
        Sample sampleNormalizzato = new Sample(featuresNormalizzate, label);

        rawDataset.addSample(rawSample);
        datasetNormalizzato.addSample(sampleNormalizzato);

        if (isDriving) {
            driverDataset.addSample(sampleNormalizzato);
        } else {
            recoveryDataset.addSample(sampleNormalizzato);
        }
        return action;
    }


    @Override
    public void shutdown() {
        rawDataset.saveToCSV("data/raw_dataset.csv");
        datasetNormalizzato.saveToCSV("data/dataset_normalizzato.csv");
        driverDataset.saveToCSV("data/driver_dataset.csv");
        recoveryDataset.saveToCSV("data/recovery_dataset.csv");
    }


    @Override
    public void reset() {
        // Eventuale logica di reset
    }
}
