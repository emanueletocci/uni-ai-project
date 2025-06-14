package it.unisa.diem.ai.torcs.utils;

import it.unisa.diem.ai.torcs.model.*;

import java.io.File;
import java.util.*;

public class DatasetBalancer {

    public static void main(String[] args) {
        String inputPath = "data/raw_dataset.csv"; // dataset sbilanciato
        String outputPath = "data/raw_dataset_balanced.csv"; // output bilanciato

        Dataset rawDataset = Dataset.loadFromCSV(inputPath);
        List<Sample> allSamples = rawDataset.getSamples();

        // Raggruppa i sample per label
        Map<Label, List<Sample>> grouped = new HashMap<>();
        for (Sample s : allSamples) {
            grouped.computeIfAbsent(s.getLabel(), _ -> new ArrayList<>()).add(s);
        }

        // Determina la classe minoritaria (max tra i minori)
        int targetSize = grouped.values().stream().mapToInt(List::size).max().orElse(0);
        System.out.println("Target size per class: " + targetSize);

        List<Sample> balanced = new ArrayList<>();
        for (Map.Entry<Label, List<Sample>> entry : grouped.entrySet()) {
            List<Sample> group = new ArrayList<>(entry.getValue());
            int originalSize = group.size();

            // Sovracampionamento
            while (group.size() < targetSize) {
                int toCopy = Math.min(originalSize, targetSize - group.size());
                group.addAll(group.subList(0, toCopy));
            }
            balanced.addAll(group);
            System.out.printf("%-15s -> %d samples\n", entry.getKey(), group.size());
        }

        // Shuffle e salva
        Collections.shuffle(balanced);
        Dataset out = new Dataset();
        balanced.forEach(out::addSample);
        out.saveToCSV(outputPath);

        System.out.println("✅ Dataset bilanciato salvato in: " + new File(outputPath).getAbsolutePath());
    }
}
