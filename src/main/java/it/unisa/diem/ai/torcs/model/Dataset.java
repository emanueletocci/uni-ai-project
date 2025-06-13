package it.unisa.diem.ai.torcs.model;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dataset {
    private final List<Sample> samples;
    private final String firstLineOfTheFile = "SPEEDX;SPEEDY;ANGLE_TO_TRACK_AXIS;TRACK_POSITION;TRACK_EDGE_SENSOR_4;TRACK_EDGE_SENSOR_6;TRACK_EDGE_SENSOR_8;TRACK_EDGE_SENSOR_9;TRACK_EDGE_SENSOR_10;TRACK_EDGE_SENSOR_12;TRACK_EDGE_SENSOR_14;Label\n";

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
    public static Dataset loadFromCSV(String filePath){
        Dataset dataset = new Dataset();
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(dataset.firstLineOfTheFile)) continue;
                dataset.addSample(new Sample(line));
            }
            br.close();
            return dataset;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataset;
    }

    // Salva il dataset su file CSV
    public void saveToCSV(String filePath) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))){
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
