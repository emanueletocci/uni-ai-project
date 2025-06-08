package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.sensors.MessageBasedSensorModel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.actions.Action;

import it.unisa.diem.ai.torcs.utilities.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utilities.KeyInput;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.io.File;

public class HumanDriver extends Controller {

    static {
        SwingUtilities.invokeLater(() -> new ContinuousCharReaderUI());
    }

    private static final String datasetFile = generateDatasetFilename();
    private static boolean headerWritten = false;

    private static String generateDatasetFilename() {
        int i = 1;
        while (true) {
            String name = "dataset" + i + ".csv";
            File f = new File(name);
            if (!f.exists()) {
                return name;
            }
            i++;
        }
    }

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
            action.steering = 0.5f;
        }

        // Salva dati su CSV
        try (FileWriter writer = new FileWriter(datasetFile, true)) {
            if (!headerWritten) {
                writer.append("track0,track1,track2,track3,track4,track5,track6,track7,track8,track9,track10,track11,track12,track13,track14,track15,track16,track17,track18,trackPos,angle,speedX,rpm,gear,accelerate,brake,steering\n");
                headerWritten = true;
            }
            writer.append(Arrays.toString(track).replaceAll("[\\[\\] ]", "") + ",");
            writer.append(trackPos + "," + angle + "," + speedX + "," + rpm + "," + gear + ",");
            writer.append(action.accelerate + "," + action.brake + "," + action.steering + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        System.out.println("Registrazione completata su file: " + datasetFile);
    }
}