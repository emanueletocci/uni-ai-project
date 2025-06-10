package it.unisa.diem.ai.torcs.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataLogger {
    private final String filename;
    private boolean headerLightWritten = false;

    public DataLogger(String filename) {
        this.filename = filename;
        File file = new File(filename);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                System.err.println("Errore: impossibile creare la directory " + parent.getAbsolutePath());
            }
        }
    }


    // Logging leggero: solo 9 sensori selezionati
    public void log(
            double[] track,        // lunghezza 9
            double trackPos,
            double angle,
            double speedX,
            int classLabel
    ) {
        File file = new File(filename);
        // Scrivi header se necessario
        if (!headerLightWritten || !file.exists() || file.length() == 0) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
                for (int i = 0; i < track.length; i++) bw.write("track" + i + ";");
                bw.write("trackPos;angle;speedX;classLabel\n");
                headerLightWritten = true;
            } catch (IOException e) {
                System.err.println("Errore scrittura header CSV (light): " + e.getMessage());
            }
        }
        // Scrivi dati
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            for (double v : track) bw.write(v + ";");
            bw.write(trackPos + ";" + angle + ";" + speedX +  ";" + classLabel + "\n");
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV (light): " + e.getMessage());
        }
    }

    // Metodo overload: prende direttamente il vettore delle feature
    public void log(double[] featuresVector, int classLabel) {
        File file = new File(filename);

        // Controllo validità vettore
        if (featuresVector.length != 12) {
            System.err.println("Errore: il vettore delle features deve contenere 12 elementi (9 track, trackPos, angle, speedX).");
            return;
        }

        // Scrivi header se necessario
        if (!headerLightWritten || !file.exists() || file.length() == 0) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
                for (int i = 0; i < 9; i++) bw.write("track" + i + ";");
                bw.write("trackPos;angle;speedX;classLabel\n");
                headerLightWritten = true;
            } catch (IOException e) {
                System.err.println("Errore scrittura header CSV (light): " + e.getMessage());
            }
        }

        // Scrivi dati
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            for (double v : featuresVector) {
                bw.write(v + ";");
            }
            bw.write(classLabel + "\n");
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV (light): " + e.getMessage());
        }
    }

}
