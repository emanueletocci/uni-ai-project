package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;
import it.unisa.diem.ai.torcs.utilities.RadarVisualizer;

import javax.swing.*;

/**
 * AutonomousDriver è un controller autonomo per TORCS che utilizza un classificatore KNN
 * per la guida standard e una logica deterministica per il recupero in caso di blocco o uscita di pista.
 *
 * - In condizioni normali, la guida è gestita tramite KNN.
 * - Se il veicolo è bloccato (stuck), viene attivata una procedura di recupero avanzata.
 * - Se il veicolo è fuori pista, viene applicata una correzione gentile per riportarlo verso il centro carreggiata.
 */
public class AutonomousDriver extends BaseDriver {
    /** Classificatore KNN per la scelta dell'azione di guida. */
    private final NearestNeighbor knn;

    /** Visualizzatore radar per il debug dei sensori. */
    private static final RadarVisualizer radar = new RadarVisualizer();

    static {
        SwingUtilities.invokeLater(() -> RadarVisualizer.showRadar(radar));
    }

    /**
     * Costruttore di AutonomousDriver.
     * Inizializza il classificatore KNN e l'oggetto Action.
     */
    public AutonomousDriver() {
        super();
        knn = new NearestNeighbor("data/dataset.csv");
    }

    /**
     * Metodo principale di controllo chiamato ad ogni ciclo di simulazione.
     *
     * - Se il veicolo è bloccato (stuck), attiva la procedura di recupero avanzata.
     * - Se il veicolo è fuori pista, applica una correzione gentile di allineamento.
     * - Altrimenti, guida standard tramite KNN.
     *
     * @param sensors Il modello sensoriale corrente fornito dal simulatore.
     * @return L'azione da eseguire in questo ciclo.
     */
    @Override
    public Action control(SensorModel sensors) {
        double trackPosition = sensors.getTrackPosition();

        if (Math.abs(sensors.getAngleToTrackAxis()) > stuckAngle) {
            stuck++;
        } else {
            stuck = 0;
        }

        // Recupero della traiettoria se i sensori rilevano anomalie o se il veicolo è fuori pista
            // Auto Bloccata: se il contatore supera la soglia, attiva il recupero
            if(stuck > stuckTime) {
                return handleRecovery(sensors);
            }
            if(isOffTrack(trackPosition)) {
                return alignToCenter(sensors);
            }

        return knnDriving(sensors);
    }


    /**
     * Gestisce la logica di recupero avanzata in caso di blocco.
     * Se il veicolo è considerato bloccato per più cicli, applica una procedura di sblocco
     * tramite retromarcia e correzione dello sterzo.
     *
     * @param sensors Il modello sensoriale corrente.
     * @return L'azione correttiva da eseguire.
     */
    private Action handleRecovery(SensorModel sensors) {
        Action action = new Action();
        // Esempio: attiva la recovery se il contatore supera la soglia
        float steer = (float) (-sensors.getAngleToTrackAxis() / steerLock);
        int gear = -1;
        if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
            gear = 1;
            steer = -steer;
        }
        clutch = clutching(sensors, clutch);
        action.gear = gear;
        action.steering = steer;
        action.accelerate = 1.0;
        action.brake = 0;
        action.clutch = clutch;

        return action;
    }

    /**
     * Gestisce la correzione gentile quando il veicolo è fuori pista.
     * Calcola una correzione progressiva dello sterzo per riportare l'auto verso il centro carreggiata.
     *
     * @param sensors Il modello sensoriale corrente.
     * @return L'azione di correzione da eseguire.
     */
    private Action alignToCenter(SensorModel sensors) {
        Action action = new Action();
        double position = sensors.getTrackPosition(); // 0: centro, -1: bordo destro, +1: bordo sinistro

        action.steering = -Math.signum(position);
        action.accelerate = 0.3;
        action.brake = 0.0;
        action.gear = sensors.getGear();
        action.clutch = 0;
        return action;
    }

    /**
     * Gestisce la logica di guida standard utilizzando il classificatore KNN.
     * Estrae e normalizza le feature dai sensori, classifica lo stato e applica l'azione predetta.
     *
     * @param sensors Il modello sensoriale corrente.
     * @return L'azione da eseguire secondo la logica di guida standard.
     */
    private Action knnDriving(SensorModel sensors) {
        Action action = new Action();

        // Lettura sensori principali
        double angle = sensors.getAngleToTrackAxis();
        double position = sensors.getTrackPosition();
        double speedX = sensors.getSpeed();
        double speedY = sensors.getLateralSpeed();
        double[] trackEdgeSensors = sensors.getTrackEdgeSensors();
        radar.updateSensors(trackEdgeSensors);

        // Estrazione e normalizzazione delle feature
        double[] features = FeatureNormalizer.extractAndNormalizeFeatures(
                trackEdgeSensors,
                position,
                angle,
                speedX,
                speedY
        );

        // Classificazione tramite KNN
        int predictedClass = knn.classify(new Sample(features), 1);
        ClassLabel label = ClassLabel.fromCode(predictedClass);

        // Azione in base alla classe predetta (allineata al dataset semplificato)
        switch (label) {
            case ACCELERA:
                accelera(action, sensors);
                break;
            case GIRA_SINISTRA:
                giraSinistra(action);
                break;
            case GIRA_DESTRA:
                giraDestra(action);
                break;
            case FRENA:
                frena(action, sensors);
                break;
            case RETROMARCIA:
                retromarcia(action, sensors);
                break;
            case MANTIENI_VELOCITA:
                mantieniVelocita(action);
                break;
            default:
                System.out.println("ERROR: unknown label " + label);
                break;
        }

        // Cambio marcia e frizione automatico
        action.gear = getGear(sensors);
        action.clutch = clutching(sensors, (float) action.clutch);
        return action;
    }
}
