package it.unisa.diem.ai.torcs;


import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.model.Dataset;
import it.unisa.diem.ai.torcs.model.Sample;

public class Main {
    public static void main(String[] args) {
        // Carica il dataset e dividi in training/test set
        Dataset dataset = Dataset.loadFromCSV("data/dataset.csv");
        dataset.shuffle();
        Dataset[] split = dataset.split(0.8); // 80% training, 20% test
        Dataset trainingSet = split[0];
        Dataset testSet = split[1];

        // Crea il classificatore con il training set
        NearestNeighbor knn = new NearestNeighbor(trainingSet);

        // Valuta l'accuratezza sul test set
        int correct = 0;
        for (Sample testSample : testSet.getSamples()) {
            int predictedClass = knn.classify(testSample, 3); // k=3
            if (predictedClass == testSample.getLabel().getCode()) {
                correct++;
            }
        }
        double accuracy = (double) correct / testSet.size();
        System.out.println("Accuracy: " + accuracy);
    }
}


