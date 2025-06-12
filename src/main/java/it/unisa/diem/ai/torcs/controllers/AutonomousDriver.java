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
    /** Oggetto Action che rappresenta l'azione corrente da eseguire. */
    private final Action action;
    /** Contatore dei giri completati fuori pista (per debug/statistiche). */
    private int giriFuoriPista = 0;

    // Parametri per la logica di recupero
    /** Contatore dei cicli consecutivi in cui l'auto è bloccata. */
    private int stuckCounter = 0;
    /** Numero massimo di cicli prima di attivare la procedura di recupero avanzata. */
    private static final int MAX_STUCK_CYCLES = 40;
    /** Soglia dell'angolo (in radianti) oltre la quale l'auto è considerata bloccata. */
    private static final double STUCK_ANGLE = Math.PI / 4; // 45 gradi
    /** Soglia della velocità (in m/s) sotto la quale l'auto è considerata bloccata. */
    private static final double STUCK_SPEED = 5.0;

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
        action = new Action();
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
        if (isStuck(sensors)) {
            return handleRecovery(sensors);
        }
        if (isOffTrack(sensors.getTrackPosition())) {
            return handleOffTrack(sensors);
        }
        return normalDriving(sensors);
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
        stuckCounter++;
        Action recoveryAction = new Action();

        // Esempio: attiva la recovery se il contatore supera la soglia
        if (stuckCounter > MAX_STUCK_CYCLES) {
            System.out.println("⚠️ Attivazione procedura di recupero avanzata");
            float steerLock = 1.0f; // Valore da calibrare in base al veicolo
            float steer = (float) (-sensors.getAngleToTrackAxis() / steerLock);
            int gear = -1; // retromarcia
            if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
                gear = 1; // prima marcia se orientato correttamente
                steer = -steer;
            }
            float clutch = 0; // Da gestire in base alla funzione clutching
            recoveryAction.gear = gear;
            recoveryAction.steering = steer;
            recoveryAction.accelerate = 1.0;
            recoveryAction.brake = 0.0;
            recoveryAction.clutch = clutch;
        }
        return recoveryAction;
    }

    /**
     * Gestisce la correzione gentile quando il veicolo è fuori pista.
     * Calcola una correzione progressiva dello sterzo per riportare l'auto verso il centro carreggiata.
     *
     * @param sensors Il modello sensoriale corrente.
     * @return L'azione di correzione da eseguire.
     */
    private Action handleOffTrack(SensorModel sensors) {
        double position = sensors.getTrackPosition(); // 0: centro, -1: bordo destro, +1: bordo sinistro
        double angle = sensors.getAngleToTrackAxis(); // angolo rispetto all'asse pista

        // Calcola una correzione proporzionale a posizione e angolo
        double correction = -position * 0.5 - angle * 0.7;
        correction = Math.max(-1, Math.min(1, correction)); // Normalizza tra -1 e 1

        action.steering = correction;
        action.accelerate = 0.3;
        action.brake = 0.0;
        action.gear = sensors.getGear() > 1 ? 1 : sensors.getGear();
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
    private Action normalDriving(SensorModel sensors) {
        stuckCounter = 0;

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

        if (isOffTrack(position)) {
            giriFuoriPista++;
            System.out.println("🚨 ATTENZIONE: Auto fuori pista! Giro: " + giriFuoriPista);
        }

        // Cambio marcia automatico
        action.gear = getGear(sensors);
        return action;
    }

    /**
     * Determina se il veicolo è bloccato in base all'angolo rispetto all'asse pista
     * e alla velocità longitudinale.
     *
     * @param sensors Il modello sensoriale corrente.
     * @return true se il veicolo è considerato bloccato, false altrimenti.
     */
    private boolean isStuck(SensorModel sensors) {
        return Math.abs(sensors.getAngleToTrackAxis()) > STUCK_ANGLE &&
                sensors.getSpeed() < STUCK_SPEED;
    }
}
