package it.unisa.diem.ai.torcs.utils;

import it.unisa.diem.ai.torcs.model.*;

import java.io.File;
import java.util.*;

public class DatasetBalancer {

    public static void main(String[] args) {
        String inputPath = "data/raw_dataset.csv";
        String outputPath = "data/filtered_dataset.csv";

        Dataset dataset = Dataset.loadFromCSV(inputPath);
        List<Sample> filtered = new ArrayList<>();

        for (Sample s : dataset.getSamples()) {
            double trackPos = s.getFeature().getValues().get(SensorFeature.TRACK_POSITION.ordinal());
            double angle = s.getFeature().getValues().get(SensorFeature.ANGLE_TO_TRACK_AXIS.ordinal());

            // Filtra fuori pista (oltre 90% margine) o angoli assurdi
            if (Math.abs(trackPos) > 0.9 || Math.abs(angle) > 0.4) continue;
            filtered.add(s);
        }

        // Raggruppa per label
        Map<Label, List<Sample>> grouped = new HashMap<>();
        for (Sample s : filtered) {
            grouped.computeIfAbsent(s.getLabel(), _ -> new ArrayList<>()).add(s);
        }

        // Clippiamo solo le accelerazioni (limite max per questa label)
        int accelLimit = 2000;
        List<Sample> finalSet = new ArrayList<>();

        for (Map.Entry<Label, List<Sample>> entry : grouped.entrySet()) {
            List<Sample> samples = entry.getValue();
            Collections.shuffle(samples);

            if (entry.getKey() == Label.ACCELERA && samples.size() > accelLimit) {
                samples = samples.subList(0, accelLimit);
            }

            finalSet.addAll(samples);
            System.out.printf("%-15s => %d samples\n", entry.getKey(), samples.size());
        }

        Collections.shuffle(finalSet);
        Dataset out = new Dataset();
        finalSet.forEach(out::addSample);
        out.saveToCSV(outputPath);

        System.out.println("✅ Dataset filtrato con clipping su ACCELERA salvato in: " + new File(outputPath).getAbsolutePath());
    }
}
