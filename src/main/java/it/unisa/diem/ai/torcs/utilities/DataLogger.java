package it.unisa.diem.ai.torcs.utilities;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.model.FeatureType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public void log(String data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
             bw.append(data);
            bw.append("\n");
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV: " + e.getMessage());
        }
    }
}
