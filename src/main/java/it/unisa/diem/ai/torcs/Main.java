package it.unisa.diem.ai.torcs;


import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.model.Dataset;
import it.unisa.diem.ai.torcs.model.Sample;

public class Main {
    public static void main(String[] args) {
        // Carica il dataset e dividi in training/test set
        Dataset dataset = Dataset.loadFromCSV("data/raw_dataset.csv");
        dataset.datasetNormalizer("data/dataset_normalizzato.csv");
    }
}


