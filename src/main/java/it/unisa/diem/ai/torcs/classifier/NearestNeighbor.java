package it.unisa.diem.ai.torcs.classifier;

import it.unisa.diem.ai.torcs.Sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NearestNeighbor {

    private List<Sample> trainingData;
    private KDTree kdtree;
    private int[] classCounts; // Assuming classes are labeled 0-9
    private String firstLineOfTheFile;

    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.kdtree = null;
        this.firstLineOfTheFile = "speed;trackPosition;trackEdgeSensor4;trackEdgeSensor6;trackEdgeSensor8;trackEdgeSensor9;"
                + "trackEdgeSensor10;trackEdgeSensor12;trackEdgeSensor14;angleToTrackAxis;classLabel";
        this.classCounts = new int[10]; // Adjust if you have a different number of classes
        this.readPointsFromCSV(filename);
    }

    private void readPointsFromCSV(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(firstLineOfTheFile)) {
                    continue; // Skip header
                }
                try {
                    if (line.trim().isEmpty()) continue; // Salta righe vuote
                    trainingData.add(new Sample(line));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.kdtree = new KDTree(trainingData);
    }

    public List<Sample> findKNearestNeighbors(Sample testPoint, int k) {
        return kdtree.kNearestNeighbors(testPoint, k);
    }

    public int classify(Sample testPoint, int k) {
        List<Sample> kNearestNeighbors = findKNearestNeighbors(testPoint, k);

        // Reset class counts
        for (int i = 0; i < classCounts.length; i++) {
            classCounts[i] = 0;
        }

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
