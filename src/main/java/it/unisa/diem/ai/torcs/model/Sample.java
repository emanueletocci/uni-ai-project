package it.unisa.diem.ai.torcs.model;

import java.util.ArrayList;
import java.util.List;

public class Sample {
    private final Feature feature;
    private final Label label;

    public Sample(Feature feature, Label label) {
        this.feature = feature;
        this.label = label;
    }

    /**
     * Crea un Sample da una riga CSV.
     * Formato atteso: f1,f2,...,fn,label
     */
    public Sample(String csvLine) {
        String[] tokens = csvLine.split(";");
        List<Double> features = new ArrayList<>();
        for (int i = 0; i < tokens.length - 1; i++) {
            features.add(Double.valueOf(Double.parseDouble(tokens[i].trim())));
        }
        this.feature = new Feature(features);
        this.label = Label.fromCode(Integer.parseInt(tokens[tokens.length - 1].trim()));
    }

    public Feature getFeature() {
        return feature;
    }

    public Label getLabel() {
        return label;
    }

    /**
     * Calcola la distanza euclidea tra questo Sample e un altro Sample.
     */
    public double distanzaEuclidea(Sample altro) {
        return this.feature.distanzaEuclidea(altro.getFeature());
    }



    /**
     * Restituisce la rappresentazione CSV di questo Sample.
     * Formato: f1,f2,...,fn,label
     */
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        List<Double> vals = feature.getValues();
        for (Double val : vals) {
            sb.append(val);
            sb.append(";");
        }
        sb.append(label.getCode());
        return sb.toString();
    }
}
