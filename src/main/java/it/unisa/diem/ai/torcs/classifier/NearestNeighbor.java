package it.unisa.diem.ai.torcs.classifier;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.model.FeatureType;

import java.io.*;
import java.util.*;

/**
 * Implementa un classificatore K-Nearest Neighbors (KNN) ottimizzato con KDTree
 * per la ricerca efficiente dei vicini più prossimi in spazi multidimensionali.
 *
 * <p>Il classificatore viene addestrato su un dataset di esempi etichettati,
 * memorizzati in un file CSV, e può classificare nuovi campioni basandosi
 * sulla maggioranza delle classi dei k vicini più prossimi.</p>
 */
public class NearestNeighbor {
    private final List<Sample> trainingData;
    private KDTree kdtree;
    private final int[] classCounts;

    /**
     * Costruisce un classificatore KNN e carica i dati di training da un file CSV.
     *
     * @param filename percorso del file CSV contenente i dati di training
     */
    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.classCounts = new int[ClassLabel.values().length];
        this.readPointsFromCSV(filename);
    }

    /**
     * Legge i dati di training da un file CSV e costruisce il KDTree.
     *
     * @param filename percorso del file CSV
     */
    private void readPointsFromCSV(String filename) {
        int expectedFeatures = FeatureType.values().length + 1; // Feature + label
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine(); // Ignora header
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.contains("track")) continue;

                String[] parts = line.split(";");
                if (parts.length != expectedFeatures) {
                    System.err.println("Riga scartata: " + line);
                    continue;
                }

                try {
                    trainingData.add(new Sample(line));
                } catch (NumberFormatException e) {
                    System.err.println("Errore in riga: " + line);
                }
            }
            this.kdtree = new KDTree(trainingData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Trova i k vicini più prossimi a un campione di test usando il KDTree.
     *
     * @param testPoint campione da classificare
     * @param k numero di vicini da considerare
     * @return lista ordinata dei k vicini più prossimi
     */
    public List<Sample> findKNearestNeighbors(Sample testPoint, int k) {
        return kdtree.kNearestNeighbors(testPoint, k);
    }

    /**
     * Classifica un campione di test in base al voto a maggioranza dei k vicini.
     *
     * @param testPoint campione da classificare
     * @param k numero di vicini da considerare
     * @return codice numerico della classe predetta
     */
    public int classify(Sample testPoint, int k) {
        List<Sample> kNearestNeighbors = findKNearestNeighbors(testPoint, k);

        Arrays.fill(classCounts, 0); // Reset conteggi

        for (Sample neighbor : kNearestNeighbors) {
            classCounts[neighbor.label.getCode()]++;
        }

        int maxCount = -1;
        int predictedClass = -1;
        for (int i = 0; i < classCounts.length; i++) {
            if (classCounts[i] > maxCount) {
                maxCount = classCounts[i];
                predictedClass = i;
            }
        }

        return predictedClass;
    }

    /**
     * Restituisce l'insieme dei dati di training.
     *
     * @return lista non modificabile dei campioni di training
     */
    public List<Sample> getTrainingData() {
        return trainingData;
    }
}
