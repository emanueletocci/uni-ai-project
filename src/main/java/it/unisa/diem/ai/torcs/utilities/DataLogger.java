package it.unisa.diem.ai.torcs.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * DataLogger che scrive l'header una sola volta, solo alla creazione del file.
 * Usa FeatureType per l'header e l'ordine delle feature.
 */
public class DataLogger {
    private final String filename;

    public DataLogger(String filename) {
        this.filename = filename;
        File file = new File(filename);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                System.err.println("Errore: impossibile creare la directory " + parent.getAbsolutePath());
            }
        }
        // Scrivi header solo se il file non esiste
        if (!file.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(FeatureType.getCSVHeader());
                bw.newLine();
            } catch (IOException e) {
                System.err.println("Errore scrittura header CSV: " + e.getMessage());
            }
        }
    }

    /**
     * Logga un vettore di feature (ordine: FeatureType.values()) e la classe.
     */
    public void log(double[] featuresVector, int classLabel) {
        if (featuresVector.length != FeatureType.values().length) {
            System.err.println("Errore: il vettore delle feature deve contenere " + FeatureType.values().length + " elementi.");
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            for (double v : featuresVector) {
                bw.write(v + ";");
            }
            bw.write(classLabel + "\n");
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV: " + e.getMessage());
        }
    }
}
