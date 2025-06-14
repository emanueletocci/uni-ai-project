package it.unisa.diem.ai.torcs.utils;

import it.unisa.diem.ai.torcs.model.*;

import java.io.File;
import java.util.*;

public class ClipBalancedDataset {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java ClipBalancedDataset <input_csv> <output_csv>");
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];

        Dataset dataset = Dataset.loadFromCSV(inputPath);

        // Raggruppa per label
        Map<Label, List<Sample>> grouped = new HashMap<>();
        for (Sample s : dataset.getSamples()) {
            grouped.computeIfAbsent(s.getLabel(), _ -> new ArrayList<>()).add(s);
        }

        // Clippiamo tutte le classi con limite massimo
        int maxPerClass = 7500; // Limite massimo di campioni per classe
        List<Sample> finalSet = new ArrayList<>();

        for (Map.Entry<Label, List<Sample>> entry : grouped.entrySet()) {
            List<Sample> samples = entry.getValue();
            Collections.shuffle(samples);
            int clipSize = Math.min(samples.size(), maxPerClass);
            finalSet.addAll(samples.subList(0, clipSize));
            System.out.printf("%-15s => %d samples\n", entry.getKey(), clipSize);
        }

        Collections.shuffle(finalSet);
        Dataset out = new Dataset();
        finalSet.forEach(out::addSample);
        out.saveToCSV(outputPath);

        System.out.println("✅ Dataset clippato salvato in: " + new File(outputPath).getAbsolutePath());
    }
}
