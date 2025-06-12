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
    this.classCounts = new int[FeatureType.values().length];
    this.trainingData = readTrainingData(filename);
    this.kdtree = new KDTree(trainingData);
}

    private List<Sample> readTrainingData(String filename) {
    List<Sample> data = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(FeatureType.getCSVHeader())) {
                continue; //salto header
            }
            try {
                data.add(Sample.fromString(line)); // o DataSample.fromString(line)
            } catch (NumberFormatException ignore) {
                System.err.println("Linea ignorata: " + line);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    return data;
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

        // Reset class counts
        for (int i = 0; i < classCounts.length; i++) {
            classCounts[i] = 0;
        }

        // Count the occurrences of each class in the k nearest neighbors
        for (Sample neighbor : kNearestNeighbors) {
            classCounts[neighbor.dataClass]++;
        }

        // Find the class with the maximum count
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