package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.*;
import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.utils.FeatureExtractor;
import it.unisa.diem.ai.torcs.model.KeyInput;

import it.unisa.diem.ai.torcs.io.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utils.FeatureNormalizer;
import it.unisa.diem.ai.torcs.utils.debugging.RadarVisualizer;

import javax.swing.*;

/**
 * Controller che consente di guidare l'auto in TORCS tramite input da tastiera.
 * Registra anche i dati di guida in tempo reale per la generazione di dataset supervisionati.
 */
public class HumanDriver extends BaseDriver {

    /** Contatore per limitare la frequenza dei campioni "ACCELERA_DRITTO". */
    private int drittoCounter = 0;

    /** Visualizzatore radar per debug visivo. */
    private static final RadarVisualizer radar = new RadarVisualizer();

    static {
        // Avvio UI per input da tastiera e radar
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
        SwingUtilities.invokeLater(() -> RadarVisualizer.showRadar(radar));
    }

    /** Dataset grezzo con feature non normalizzate. */
    private final Dataset rawDataset;

    /** Dataset normalizzato. */
    private final Dataset datasetNormalizzato;

    /** Dataset contenente esempi di recovery (fuori traiettoria). */
    private final Dataset recoveryDataset;

    /** Dataset contenente esempi di guida corretta. */
    private final Dataset driverDataset;

    /** Estrattore di feature dai sensori. */
    private final FeatureExtractor extractor;

    /** Normalizzatore delle feature. */
    private final FeatureNormalizer normalizer;

    /**
     * Costruttore che inizializza i dataset e i moduli di estrazione/normalizzazione.
     */
    public HumanDriver() {
        rawDataset = new Dataset();
        datasetNormalizzato = new Dataset();
        recoveryDataset = new Dataset();
        driverDataset = new Dataset();
        extractor = new FeatureExtractor();
        normalizer = new FeatureNormalizer();
    }

    /**
     * Metodo principale di controllo del veicolo, basato su input utente.
     * Interpreta i tasti premuti e costruisce l'oggetto {@link Action} corrispondente.
     * Aggiorna anche i radar, registra i dati se richiesto.
     *
     * @param sensors il modello dei sensori con lo stato attuale dell’auto
     * @return azione da eseguire nel simulatore
     */
    @Override
    public Action control(SensorModel sensors) {
        radar.updateSensors(sensors.getTrackEdgeSensors());

        Action action = new Action();
        double speedX = sensors.getSpeed();

        // Gestione accelerazione, frenata e retromarcia
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

        // Gestione sterzo
        if (KeyInput.left && !KeyInput.right) {
            giraSinistra(action, sensors);
        } else if (KeyInput.right && !KeyInput.left) {
            giraDestra(action, sensors);
        } else {
            action.steering = 0.0;
        }

        // Cambio automatico marcia se non in retromarcia
        if (action.gear != -1) {
            action.gear = getGear(sensors);
        }

        // Frizione e ABS
        action.clutch = clutching(sensors, (float) action.clutch);
        if (action.brake > 0) {
            action.brake = filterABS(sensors, (float) action.brake);
        }

        // --- Registrazione nel dataset ---

        Label label = Label.fromAction(action);

        // Riduzione esempi "ACCELERA" dritti per evitare sbilanciamento
        if (label == Label.ACCELERA) {
            drittoCounter++;
            if (drittoCounter % 5 != 0) {
                return action; // Salta la registrazione per 4 su 5
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

        // Registra solo se la UI lo consente (checkbox attiva)
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

    /**
     * Metodo chiamato alla chiusura della simulazione.
     * Salva i dataset raccolti su file CSV.
     */
    @Override
    public void shutdown() {
        rawDataset.saveToCSV("data/raw_dataset.csv");
        datasetNormalizzato.saveToCSV("data/dataset_normalizzato.csv");
        driverDataset.saveToCSV("data/driver_dataset.csv");
        // recoveryDataset.saveToCSV("data/recovery_dataset.csv");
    }

    /**
     * Metodo chiamato a ogni reset del simulatore.
     * Può essere sovrascritto per logica personalizzata.
     */
    @Override
    public void reset() {
        // Eventuale logica di reset se necessaria
    }
}
