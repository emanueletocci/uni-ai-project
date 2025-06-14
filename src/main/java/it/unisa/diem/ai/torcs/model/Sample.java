package it.unisa.diem.ai.torcs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Rappresenta un campione etichettato composto da un vettore di feature e da una {@link Label}.
 * Ogni {@code Sample} può essere caricato da una riga CSV o costruito direttamente da oggetti.
 */
public class Sample {

    /** Vettore delle caratteristiche (features) del campione */
    private final FeatureVector feature;

    /** Etichetta associata al campione (classe) */
    private final Label label;

    /**
     * Costruttore base: crea un nuovo Sample con feature e label specificate.
     * @param feature vettore delle feature
     * @param label etichetta associata
     */
    public Sample(FeatureVector feature, Label label) {
        this.feature = feature;
        this.label = label;
    }

    /**
     * Costruttore che crea un Sample a partire da una riga CSV.
     * <p>Formato atteso: {@code f1;f2;...;fn;labelCode;labelName}</p>
     * La label testuale alla fine viene ignorata.
     * @param csvLine riga del file CSV
     * @throws NumberFormatException se il parsing dei valori fallisce
     */
    public Sample(String csvLine) {
        String[] tokens = csvLine.split(";");
        List<Double> features = new ArrayList<>();
        for (int i = 0; i < tokens.length - 2; i++) {
            String sanitized = tokens[i].trim().replace(',', '.');
            features.add(Double.parseDouble(sanitized));
        }
        this.feature = new FeatureVector(features);
        this.label = Label.fromCode(Integer.parseInt(tokens[tokens.length - 2].trim()));
    }

    /**
     * @return vettore delle feature del campione
     */
    public FeatureVector getFeature() {
        return feature;
    }

    /**
     * @return etichetta del campione
     */
    public Label getLabel() {
        return label;
    }

    /**
     * Calcola la distanza euclidea tra questo Sample e un altro.
     * @param altro l'altro campione da confrontare
     * @return distanza euclidea tra i due campioni
     */
    public double distanzaEuclidea(Sample altro) {
        return this.feature.distanzaEuclidea(altro.getFeature());
    }

    /**
     * Converte questo Sample in una riga CSV.
     * <p>Formato: {@code f1;f2;...;fn;labelCode;labelName}</p>
     * @return stringa CSV del campione
     */
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        for (Double val : feature.getValues()) {
            sb.append(String.format(Locale.ITALY, "%.5f", val));
            sb.append(";");
        }
        sb.append(label.getCode()).append(";").append(label);
        return sb.toString();
    }
}
