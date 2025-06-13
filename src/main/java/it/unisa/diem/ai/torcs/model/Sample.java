package it.unisa.diem.ai.torcs.model;

import java.util.ArrayList;
import java.util.List;

public class Sample {
    private final FeatureVector feature;
    private final Label label;

    public Sample(FeatureVector feature, Label label) {
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
            features.add(Double.parseDouble(tokens[i].trim()));
        }
        this.feature = new FeatureVector(features);
        this.label = Label.fromCode(Integer.parseInt(tokens[tokens.length - 1].trim()));
    }

    public FeatureVector getFeature() {
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
        for (Double val : feature.getValues()) {
            sb.append(val);
            sb.append(";");
        }
        sb.append(label.getCode());
        sb.append(";");
        sb.append(label);
        return sb.toString();
    }
}
