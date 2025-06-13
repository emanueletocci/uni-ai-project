package it.unisa.diem.ai.torcs.model;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dataset {
    private final List<Sample> samples;
    private final String FIRST_FILE_LINE = SensorFeature.csvHeader();
    public Dataset() {
        this.samples = new ArrayList<>();
    }

    public void addSample(Sample sample) {
        samples.add(sample);
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public int size() {
        return samples.size();
    }

    // Carica il dataset da file CSV
    public static Dataset loadFromCSV(String filePath) {
        Dataset dataset = new Dataset();
        String expectedHeader = SensorFeature.csvHeader();
        int expectedColumns = expectedHeader.split(";").length;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue; // Skip empty lines
                if (lineNumber == 1) {
                    // Skip header, but optionally check it
                    if (!line.equals(expectedHeader)) {
                        System.err.println("Warning: header does not match expected format!");
                    }
                    continue;
                }
                String[] tokens = line.split(";");
                if (tokens.length != expectedColumns) {
                    System.err.println("Skipping malformed line " + lineNumber + ": wrong number of columns (" + tokens.length + " instead of " + expectedColumns + ")");
                    continue;
                }
                try {
                    dataset.addSample(new Sample(line));
                } catch (Exception e) {
                    System.err.println("Skipping malformed line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataset;
    }


    // Salva il dataset su file CSV
    public void saveToCSV(String filePath) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))){
            bw.write(FIRST_FILE_LINE);
            bw.newLine();
            for (Sample sample : samples) {
                bw.write(sample.toCSV());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Shuffle del dataset
    public void shuffle() {
        Collections.shuffle(samples);
    }

    // Split in training e test set (percentuale tra 0 e 1)
    public Dataset[] split(double trainRatio) {
        int trainSize = (int) (samples.size() * trainRatio);
        List<Sample> train = new ArrayList<>(samples.subList(0, trainSize));
        List<Sample> test = new ArrayList<>(samples.subList(trainSize, samples.size()));
        Dataset[] result = {new Dataset(), new Dataset()};
        for (Sample s : train) result[0].addSample(s);
        for (Sample s : test) result[1].addSample(s);
        return result;
    }
}
