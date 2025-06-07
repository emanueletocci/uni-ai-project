package it.unisa.diem.ai.torcs.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataLogger {
    private final String filename;
    private boolean headerWritten = false;

    public DataLogger(String filename) {
        this.filename = filename;
        File file = new File(filename);

        // Crea la directory se non esiste
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        // Scrivi header solo se il file è nuovo o vuoto
        if (!file.exists() || file.length() == 0) {
            writeHeader();
            headerWritten = true;
        }
    }

    private void writeHeader() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            bw.write("speed;trackPosition;trackEdgeSensor4;trackEdgeSensor6;trackEdgeSensor8;trackEdgeSensor9;trackEdgeSensor10;trackEdgeSensor12;trackEdgeSensor14;angleToTrackAxis;classLabel\n");
        } catch (IOException e) {
            System.err.println("Errore scrittura header CSV: " + e.getMessage());
        }
    }

    public void log(double speed, double trackPosition,
                    double[] trackEdgeSensors, // lunghezza 19
                    double angleToTrackAxis, int classLabel) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            bw.write(
                    speed + ";" +
                            trackPosition + ";" +
                            trackEdgeSensors[4] + ";" +
                            trackEdgeSensors[6] + ";" +
                            trackEdgeSensors[8] + ";" +
                            trackEdgeSensors[9] + ";" +
                            trackEdgeSensors[10] + ";" +
                            trackEdgeSensors[12] + ";" +
                            trackEdgeSensors[14] + ";" +
                            angleToTrackAxis + ";" +
                            classLabel + "\n"
            );
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV: " + e.getMessage());
        }
    }
}
