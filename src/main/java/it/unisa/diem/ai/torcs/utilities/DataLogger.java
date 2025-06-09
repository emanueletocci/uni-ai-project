package it.unisa.diem.ai.torcs.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataLogger {
    private final String filename;
    private boolean headerFullWritten = false;
    private boolean headerLightWritten = false;

    public DataLogger(String filename) {
        this.filename = filename;
        File file = new File(filename);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    // Logging completo: tutti i 19 sensori track
    public void logFull(
            double[] track,        // lunghezza 19
            double trackPos,
            double angle,
            double speedX,
            double speedY,
            int classLabel
    ) {
        File file = new File(filename);
        // Scrivi header se necessario
        if (!headerFullWritten || !file.exists() || file.length() == 0) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
                for (int i = 0; i < 19; i++) bw.write("track" + i + ";");
                bw.write("trackPos;angle;speedX;speedY;classLabel\n");
                headerFullWritten = true;
            } catch (IOException e) {
                System.err.println("Errore scrittura header CSV (full): " + e.getMessage());
            }
        }
        // Scrivi dati
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            for (int i = 0; i < 19; i++) bw.write(track[i] + ";");
            bw.write(trackPos + ";" + angle + ";" + speedX + ";" + speedY  + ";" + classLabel + "\n");
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV (full): " + e.getMessage());
        }
    }

    // Logging leggero: solo 9 sensori
    public void logLight(
            double[] track,        // lunghezza 9
            double trackPos,
            double angle,
            double speedX,
            int classLabel
    ) {
        File file = new File(filename);
        // Indici scelti: 0 (estrema sinistra), 4 (sinistra), 9 (centro), 14 (destra), 18 (estrema destra)
        int[] selected = {0, 3, 4, 8, 9, 10, 14, 15, 18};
        // Scrivi header se necessario
        if (!headerLightWritten || !file.exists() || file.length() == 0) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
                for (int idx : selected) bw.write("track" + idx + ";");
                bw.write("trackPos;angle;speedX;classLabel\n");
                headerLightWritten = true;
            } catch (IOException e) {
                System.err.println("Errore scrittura header CSV (light): " + e.getMessage());
            }
        }
        // Scrivi dati
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            for (int idx : selected) bw.write(track[idx] + ";");
            bw.write(trackPos + ";" + angle + ";" + speedX +  ";" + classLabel + "\n");
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV (light): " + e.getMessage());
        }
    }
}
