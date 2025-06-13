package it.unisa.diem.ai.torcs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        for (int i = 0; i < tokens.length - 2; i++) { // fino a LABEL_CODE escluso
            String sanitized = tokens[i].trim().replace(',', '.');
            features.add(Double.parseDouble(sanitized));
        }
        this.feature = new FeatureVector(features);
        // Prendi la label numerica dalla penultima colonna
        this.label = Label.fromCode(Integer.parseInt(tokens[tokens.length - 2].trim()));
        // Ignora completamente tokens[tokens.length - 1] (label testuale)
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
            sb.append(String.format(Locale.ITALY, "%.5f", val));
            sb.append(";");
        }
        sb.append(label.getCode());
        sb.append(";");
        sb.append(label);
        return sb.toString();
    }
}
