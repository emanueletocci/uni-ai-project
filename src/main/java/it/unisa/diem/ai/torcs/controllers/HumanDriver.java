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

    private final DataLogger fullLogger = new DataLogger("data/dataset_full.csv");
    private final DataLogger lightLogger = new DataLogger("data/dataset_light.csv");

    @Override
    public Action control(SensorModel sensors) {

        Action action = new Action();

        // Legge i sensori
        MessageBasedSensorModel model = (MessageBasedSensorModel) sensors;
        double[] track = model.getTrackEdgeSensors();
        double trackPos = model.getTrackPosition();
        double angle = model.getAngleToTrackAxis();
        double speedX = model.getSpeed();
        double rpm = model.getRPM();
        int gear = model.getGear();

        // 🔍 Debug stato tasti
        KeyInput.print();

        // Cambio automatico
        if (gear <= 3 && rpm > 7000) {
            gear++;
        } else if (gear > 1 && rpm < 3000) {
            gear--;
        } else if (gear > 3 && rpm > 8000) {
            gear++;
        }
        action.gear = gear;

        // Comandi da tastiera
        action.accelerate = KeyInput.up ? 1.0f : 0.0f;
        action.brake = KeyInput.down ? 1.0f : 0.0f;
        if (KeyInput.left) {
            action.steering = 0.5f;
        } else if (KeyInput.right) {
            action.steering = -0.5f;
        } else {
            action.steering = 0.0f;
        }

        if (KeyInput.left) {
            System.out.println("🚗 Sto cercando di sterzare a SINISTRA");
        }

        // Logging completo
        fullLogger.logFull(
                track,        // tutti i 19 sensori track
                trackPos,
                angle,
                speedX,
                rpm,
                gear,
                action.accelerate,
                action.brake,
                action.steering
        );
        // Logging leggero

        lightLogger.logLight(
                track,        // verranno selezionati solo alcuni sensori
                trackPos,
                angle,
                speedX,
                action.accelerate,
                action.brake,
                action.steering
        );

        // Debug: conferma lo stato dei comandi
        System.out.println("LEFT=" + KeyInput.left + ", RIGHT=" + KeyInput.right + ", steer=" + action.steering);

        return action;
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