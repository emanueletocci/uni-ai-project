package it.unisa.diem.ai.torcs.classifier;

import java.util.List;

import it.unisa.diem.ai.torcs.model.Dataset;
import it.unisa.diem.ai.torcs.model.Label;
import it.unisa.diem.ai.torcs.model.Sample;

import javax.xml.crypto.Data;

public class NearestNeighbor {

    private Dataset trainingData;
    private KDTree kdtree;
    private int[] classCounts;

    public NearestNeighbor(Dataset trainingData) {
        this.trainingData = trainingData;
        this.kdtree = new KDTree(trainingData.getSamples());
        this.classCounts = new int[Label.values().length];
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
            int classCode = neighbor.getLabel().getCode();
            classCounts[classCode]++;
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


    public Dataset getTrainingData() {
        return trainingData;
    }
}
