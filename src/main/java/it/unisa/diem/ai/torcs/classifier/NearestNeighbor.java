package it.unisa.diem.ai.torcs.classifier;

import it.unisa.diem.ai.torcs.Sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NearestNeighbor {

    private final List<Sample> trainingData;
    private KDTree kdtree;
    private final int[] classCounts; // Assuming classes are labeled 0-9

    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.kdtree = null;
        this.classCounts = new int[10];
        this.readPointsFromCSV(filename);
    }

    private void readPointsFromCSV(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Leggi e scarta la prima riga (header)
            String header = reader.readLine();
            if (header == null) {
                System.err.println("File vuoto!");
                return;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // Salta eventuali header ripetuti (es: merge di file)
                if (line.toLowerCase().contains("track0") || line.toLowerCase().contains("classlabel")) {
                    System.err.println("Salto header ripetuto o riga non dati: " + line);
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length != 13) { // 12 feature + 1 classLabel
                    System.err.println("Riga malformata: " + line);
                    continue;
                }
                try {
                    trainingData.add(new Sample(line));
                } catch (NumberFormatException e) {
                    System.err.println("Errore di parsing nella riga: " + line);
                }
            }

            if (trainingData.isEmpty()) {
                System.err.println("Nessun dato nel file dopo l'header!");
            }
            this.kdtree = new KDTree(trainingData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<Sample> findKNearestNeighbors(Sample testPoint, int k) {
        return kdtree.kNearestNeighbors(testPoint, k);
    }

    public int classify(Sample testPoint, int k) {
        List<Sample> kNearestNeighbors = findKNearestNeighbors(testPoint, k);

        // Reset class counts
        Arrays.fill(classCounts, 0);

        // Count the occurrences of each class in the k nearest neighbors
        for (Sample neighbor : kNearestNeighbors) {
            classCounts[neighbor.cls]++;
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

    public List<Sample> getTrainingData() {
        return trainingData;
    }
}
