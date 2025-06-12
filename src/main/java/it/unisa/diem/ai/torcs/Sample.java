package it.unisa.diem.ai.torcs;

import it.unisa.diem.ai.torcs.model.ClassLabel;

import java.util.Locale;

/**
 * La classe Sample rappresenta un singolo campione di dati,
 * composto da un vettore di feature e da una label di classe di tipo enumerato.
 * È utilizzata per compiti di classificazione e behavioral cloning.
 */
public class Sample {
    /**
     * Array di valori numerici che rappresentano le feature del campione.
     */
    public double[] features;

    /**
     * Etichetta di classe associata al campione (di tipo ClassLabel).
     */
    public ClassLabel label;

    /**
     * Costruisce un oggetto Sample dato un vettore di feature e una label di classe.
     *
     * @param features array di valori delle feature
     * @param label    etichetta di classe (ClassLabel)
     */
    public Sample(double[] features, ClassLabel label) {
        this.features = features;
        this.label = label;
    }

    /**
     * Costruisce un oggetto Sample dato solo il vettore di feature.
     * Utilizzato quando la classe non è nota o deve essere assegnata successivamente.
     *
     * @param features array di valori delle feature
     */
    public Sample(double[] features) {
        this.features = features;
        this.label = null;
    }

    /**
     * Costruisce un oggetto Sample a partire da una riga CSV.
     * Si assume che l'ultima colonna sia il codice numerico della classe.
     *
     * @param line riga del file CSV contenente feature e label, separate da punto e virgola
     */
    public Sample(String line) {
        String[] parts = line.split(";");
        int n = parts.length;
        features = new double[n - 1];
        for (int i = 0; i < n - 1; i++) {
            features[i] = Double.parseDouble(parts[i].trim());
        }
        int code = Integer.parseInt(parts[n - 1].trim());
        this.label = ClassLabel.fromCode(code);
    }

    /**
     * Calcola la distanza euclidea tra questo campione e un altro campione.
     * La distanza viene calcolata sulle feature, ignorando la label.
     *
     * @param other altro oggetto Sample con cui calcolare la distanza
     * @return distanza euclidea tra i due campioni
     */
    public double distance(Sample other) {
        double sum = 0;
        for (int i = 0; i < this.features.length; i++) {
            sum += Math.pow(this.features[i] - other.features[i], 2);
        }
        return Math.sqrt(sum);
    }
}
