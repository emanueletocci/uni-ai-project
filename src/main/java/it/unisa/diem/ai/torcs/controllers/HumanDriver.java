package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.sensors.MessageBasedSensorModel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.utilities.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utilities.DataLogger;
import it.unisa.diem.ai.torcs.utilities.KeyInput;
import javax.swing.*;

public class HumanDriver extends Controller {

    static {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }

    /* Costanti di cambio marcia */
    final int[] gearUp = {5000, 6000, 6000, 6500, 7000, 0};
    final int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};

    /* Constanti */
    final int stuckTime = 25;
    final float stuckAngle = (float) 0.523598775; // PI/6

    /* Costanti di accelerazione e di frenata */
    final float maxSpeedDist = 70;
    final float maxSpeed = 150;
    final float sin5 = (float) 0.08716;
    final float cos5 = (float) 0.99619;

    /* Costanti di sterzata */
    final float steerLock = (float) 0.785398;
    final float steerSensitivityOffset = (float) 80.0;
    final float wheelSensitivityCoeff = 1;

    /* Costanti del filtro ABS */
    final float[] wheelRadius = {(float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276};
    final float absSlip = (float) 2.0;
    final float absRange = (float) 3.0;
    final float absMinSpeed = (float) 3.0;

    /* Costanti da stringere */
    final float clutchMax = (float) 0.5;
    final float clutchDelta = (float) 0.05;
    final float clutchRange = (float) 0.82;
    final float clutchDeltaTime = (float) 0.02;
    final float clutchDeltaRaced = 10;
    final float clutchDec = (float) 0.01;
    final float clutchMaxModifier = (float) 1.3;
    final float clutchMaxTime = (float) 1.5;

    private int stuck = 0;

    // current clutch
    private float clutch = 0;

    private final DataLogger fullLogger;
    private final DataLogger lightLogger;

    public HumanDriver(){
        fullLogger = new DataLogger("data/dataset_full.csv");
        lightLogger = new DataLogger("data/dataset_light.csv");
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
        double rpm = model.getRPM();
        int gear = model.getGear();

        // 2. Gestione manuale: acceleratore, freno e retromarcia
        if (KeyInput.brake) {
            // Freno sempre, priorità massima
            action.brake = 0.8f;
            action.accelerate = 0.0f;
            // Mantieni la marcia attuale (o automatica)
        } else if (KeyInput.down && speedX < 1.0) {
            // Se S e quasi fermo → retro
            action.gear = -1;
            action.accelerate = 1.0f;
            action.brake = 0.0f;
        } else if (KeyInput.up) {
            // Accelerazione in avanti
            action.accelerate = 1.0f;
            action.brake = 0.0f;
            if (gear < 1) action.gear = 1; // Torna in prima se eri in retro
        } else {
            // Nessun comando: nessuna accelerazione né freno
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
        float steeringSensitivity = 0.3f; // Puoi modificare la sensibilità
        action.steering = Math.max(-1.0f, Math.min(1.0f, steeringInput * steeringSensitivity));

        // 5. Calcolo classLabel (opzionale, per behavioral cloning)
        int classLabel = calculateClassLabel(action);

        // 6. Logging (opzionale, puoi scegliere se usare classLabel)
        fullLogger.logFull(track, trackPos, angle, speedX, rpm, classLabel);

        lightLogger.logLight(track, trackPos, angle, speedX, classLabel);

        return action;
    }


    // Le label sono essenziali per il calssificatore. Sono sostanzialmente le features quindi dobbiamo decidere quali usare
    private int calculateClassLabel(Action action) {
        boolean avanti = action.accelerate > 0 && action.gear > 0 && action.brake == 0;
        boolean retro = action.accelerate > 0 && action.gear == -1 && action.brake == 0;
        boolean sinistra = action.steering > 0.1f;
        boolean destra = action.steering < -0.1f;
        boolean freno = action.brake > 0;

        // Più specifici prima!
        if (avanti && sinistra) return 2;
        if (avanti && destra) return 3;
        if (retro && sinistra) return 6;
        if (retro && destra) return 7;
        if (avanti) return 1;
        if (retro) return 5;
        if (freno) return 4;
        return 0; // nessuna azione
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

        // Se la marcia è 0 (N) o -1 (R), restituisci 1 (prima)
        if (gear < 1)
            return 1;

        // Sali di marcia se superi la soglia superiore
        if (gear < 6 && rpm >= gearUp[gear - 1])
            return gear + 1;

        // Scala di marcia se scendi sotto la soglia inferiore
        if (gear > 1 && rpm <= gearDown[gear - 1])
            return gear - 1;

        // Altrimenti mantieni la marcia attuale
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