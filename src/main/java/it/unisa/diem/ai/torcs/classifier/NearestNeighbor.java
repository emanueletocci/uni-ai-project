package it.unisa.diem.ai.torcs.classifier;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.model.FeatureType;

import java.io.*;
import java.util.*;

/**
 * Classificatore basato su K-Nearest Neighbors (KNN) che utilizza un KDTree
 * per velocizzare la ricerca dei vicini più prossimi.
 */
public class NearestNeighbor {
    private final List<Sample> trainingData;
    private KDTree kdtree;
    private final int[] classCounts;

    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.classCounts = new int[FeatureType.values().length]; // prelevo dinamicamente la dimensione
        this.readPointsFromCSV(filename);
    }

    private void readPointsFromCSV(String filename) {
        int expectedFeatures = FeatureType.values().length + 1; // 12 + 1
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine(); // Salta header
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
     * Trova i K vicini più prossimi a un punto dato.
     */
    public List<Sample> findKNearestNeighbors(Sample testPoint, int k) {
        return kdtree.kNearestNeighbors(testPoint, k);
    }

    /**
     * Classifica un punto test in base alla maggioranza dei K vicini.
     */
    public int classify(Sample testPoint, int k) {
        List<Sample> kNearestNeighbors = findKNearestNeighbors(testPoint, k);

        // Azzera il conteggio delle classi
        Arrays.fill(classCounts, 0);

        // Conta quante volte compare ogni classe nei vicini
        for (Sample neighbor : kNearestNeighbors) {
            classCounts[neighbor.cls]++;
        }

        // Trova la classe più frequente (maggioranza)
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
     * Restituisce i dati di training (utile per analisi/debug).
     */
    public List<Sample> getTrainingData() {
        return trainingData;
    }
}