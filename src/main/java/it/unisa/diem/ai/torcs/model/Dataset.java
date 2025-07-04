package it.unisa.diem.ai.torcs.model;

import it.unisa.diem.ai.torcs.utils.FeatureNormalizer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Rappresenta un dataset supervisionato composto da oggetti {@link Sample}.
 * Fornisce funzionalità per aggiungere campioni, salvarli/caricarli da file CSV,
 * normalizzare le feature, mescolare i dati e suddividerli in train/test set.
 */
public class Dataset {

    /** Lista di campioni nel dataset */
    private final List<Sample> samples;

    /** Riga di intestazione per file CSV (nominata come in SensorFeature) */
    private final String FIRST_FILE_LINE = SensorFeature.csvHeader();

    /** Costruttore vuoto: inizializza un dataset senza campioni */
    public Dataset() {
        this.samples = new ArrayList<>();
    }

    /**
     * Aggiunge un campione al dataset.
     *
     * @param sample il campione da aggiungere
     */
    public void addSample(Sample sample) {
        samples.add(sample);
    }

    /**
     * Restituisce la lista dei campioni nel dataset.
     *
     * @return lista dei campioni
     */
    public List<Sample> getSamples() {
        return samples;
    }

    /**
     * Restituisce il numero totale di campioni presenti nel dataset.
     *
     * @return numero di campioni
     */
    public int size() {
        return samples.size();
    }

    /**
     * Carica un dataset da file CSV.
     * Il file deve contenere un'intestazione e righe con valori separati da punto e virgola.
     *
     * @param filePath percorso al file CSV
     * @return oggetto Dataset caricato
     */
    public static Dataset loadFromCSV(String filePath) {
        Dataset dataset = new Dataset();
        String expectedHeader = SensorFeature.csvHeader();
        int expectedColumns = expectedHeader.split(";").length;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;
                if (lineNumber == 1) {
                    if (!line.equals(expectedHeader)) {
                        System.err.println("Warning: header does not match expected format!");
                    }
                    continue;
                }
                String[] tokens = line.split(";");
                if (tokens.length != expectedColumns) {
                    System.err.println("Skipping malformed line " + lineNumber + ": wrong number of columns (" + tokens.length + " instead of " + expectedColumns + ")");
                    continue;
                }
                try {
                    dataset.addSample(new Sample(line));
                } catch (Exception e) {
                    System.err.println("Skipping malformed line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataset;
    }

    /**
     * Salva il dataset in un file CSV, includendo intestazione e tutti i campioni.
     *
     * @param filePath percorso del file di destinazione
     */
    public void saveToCSV(String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(FIRST_FILE_LINE);
            bw.newLine();
            for (Sample sample : samples) {
                bw.write(sample.toCSV());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Normalizza tutte le feature del dataset e salva il nuovo dataset normalizzato su file CSV.
     *
     * @param outputPath percorso del file CSV normalizzato da generare
     */
    public void datasetNormalizer(String outputPath) {
        FeatureNormalizer normalizer = new FeatureNormalizer();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath, false))) {
            writer.write(SensorFeature.csvHeader());
            writer.newLine();

            for (Sample sample : samples) {
                FeatureVector normalized = normalizer.normalize(sample.getFeature());
                StringBuilder sb = new StringBuilder();
                for (Double val : normalized.getValues()) {
                    sb.append(String.format(Locale.US, "%.5f;", val));
                }
                sb.append(sample.getLabel().getCode()).append(";");
                sb.append(sample.getLabel().toString());
                writer.write(sb.toString());
                writer.newLine();
            }

            System.out.println("Dataset normalizzato salvato in: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mescola casualmente l'ordine dei campioni nel dataset.
     */
    public void shuffle() {
        Collections.shuffle(samples);
    }
}
