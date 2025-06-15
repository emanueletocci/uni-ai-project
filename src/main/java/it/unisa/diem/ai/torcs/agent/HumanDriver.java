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
    private int drittoCounter = 0;
    private static final RadarVisualizer radar = new RadarVisualizer();
    static {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
        SwingUtilities.invokeLater(() -> RadarVisualizer.showRadar(radar));
    }

    private final Dataset rawDataset; // Dataset grezzo
    private final Dataset datasetNormalizzato; // Dataset normalizzato
    private final Dataset recoveryDataset;
    private final Dataset driverDataset;
    private final FeatureExtractor extractor;
    private final FeatureNormalizer normalizer;

    public HumanDriver() {
        rawDataset = new Dataset();
        datasetNormalizzato = new Dataset();
        recoveryDataset = new Dataset();
        driverDataset = new Dataset();
        extractor = new FeatureExtractor();
        normalizer = new FeatureNormalizer();
    }

    @Override
    public Action control(SensorModel sensors) {
        radar.updateSensors(sensors.getTrackEdgeSensors());

        // Leggo i comandi dalla tastiera (KeyInput)
        Action action = new Action();
        double speedX = sensors.getSpeed();

        // Accelerazione, freno, retromarcia
        if (KeyInput.brake) {
            frena(action, sensors);
        } else if (KeyInput.down && speedX < 5.0) {
            retromarcia(action, sensors);
        } else if (KeyInput.up) {
            accelera(action, sensors);
        } else {
            action.accelerate = 0.0;
            action.brake = 0.0;
        }

        // Sterzo
        if (KeyInput.left && !KeyInput.right) {
            giraSinistra(action, sensors);
        } else if (KeyInput.right && !KeyInput.left) {
            giraDestra(action, sensors);
        } else {
            action.steering = 0.0;
        }

        // Cambio marcia automatico
        if (action.gear != -1) {
            action.gear = getGear(sensors);
        }

        // Gestione frizione e ABS
        action.clutch = clutching(sensors, (float) action.clutch);
        if (action.brake > 0) {
            action.brake = filterABS(sensors, (float) action.brake);
        }

        // --- Dataset ---
        Label label = Label.fromAction(action);
        // Riduci il numero di esempi ACCELERA_DRITTO per bilanciare il dataset
        if (label == Label.ACCELERA) {
            drittoCounter++;
            if (drittoCounter % 5 != 0) {
                return action; // Salta la registrazione (4 su 5)
            }
        }
        FeatureVector rawFeatures = extractor.extractFeatures(sensors);
        FeatureVector featuresNormalizzate = normalizer.normalize(rawFeatures);

        double trackPos = sensors.getTrackPosition();
        double angle = sensors.getAngleToTrackAxis();
        double speedY = sensors.getLateralSpeed();
        boolean isDriving = Math.abs(trackPos) <= 0.9 && Math.abs(angle) <= 0.5 && Math.abs(speedY) <= 15;

        Sample rawSample = new Sample(rawFeatures, label);
        Sample sampleNormalizzato = new Sample(featuresNormalizzate, label);

        // ✅ Registra solo se la checkbox è selezionata
        ContinuousCharReaderUI ui = ContinuousCharReaderUI.getInstance();
        if (ui != null && ui.isDatasetRecordingEnabled()) {
            rawDataset.addSample(rawSample);
            datasetNormalizzato.addSample(sampleNormalizzato);

            if (isDriving) {
                driverDataset.addSample(sampleNormalizzato);
            } else {
                recoveryDataset.addSample(sampleNormalizzato);
            }
        }

        return action;
    }

    @Override
    public void shutdown() {
        rawDataset.saveToCSV("data/raw_dataset.csv");
        datasetNormalizzato.saveToCSV("data/dataset_normalizzato.csv");
        //driverDataset.saveToCSV("data/driver_dataset.csv");
        recoveryDataset.saveToCSV("data/recovery_dataset.csv");
    }

    @Override
    public void reset() {
        // Eventuale logica di reset se necessaria
    }
}
