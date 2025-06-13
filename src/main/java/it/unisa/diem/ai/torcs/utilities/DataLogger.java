package it.unisa.diem.ai.torcs.utilities;

import it.unisa.diem.ai.torcs.model.FeatureType;

import java.io.*;
import java.util.Locale;

/**
 * DataLogger che scrive l'header una sola volta, solo alla creazione del file.
 * Usa FeatureType per l'header e l'ordine delle feature.
 */
public class DataLogger {
    private final String filename;

    public DataLogger(String filename) {
        this.filename = filename;
        File file = new File(filename);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                System.err.println("Errore: impossibile creare la directory " + parent.getAbsolutePath());
            }
        }
        // Scrivi header solo se il file non esiste
        if (!file.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(FeatureType.getCSVHeader());
                bw.newLine();
            } catch (IOException e) {
                System.err.println("Errore scrittura header CSV: " + e.getMessage());
            }
        }
    }

    /**
     * Logga un vettore di feature (ordine: FeatureType.values()) e la classe.
     */
    public void logFeaturesNormalizzate(double[] featuresVector, int classLabel) {
        if (featuresVector.length != FeatureType.values().length) {
            System.err.println("Errore: il vettore delle feature deve contenere " + FeatureType.values().length + " elementi.");
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            for (double v : featuresVector) {
                bw.write(String.format(Locale.ROOT, "%.4f;", v));
            }
            bw.write(classLabel + "\n");
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV: " + e.getMessage());
        }
    }

    /**
     * Logga i valori grezzi (non normalizzati) delle feature definite in FeatureType, più la classe.
     *
     * @param track     Vettore dei 19 sensori di bordo pista.
     * @param trackPos  Posizione laterale sulla pista.
     * @param angle     Angolo rispetto all’asse della pista.
     * @param speedX    Velocità longitudinale.
     * @param speedY    Velocità laterale.
     * @param classLabel Etichetta della classe da associare a queste feature.
     */
    public void logFeaturesRaw(double[] track, double trackPos, double angle, double speedX, double speedY, int classLabel) {
        double[] features = new double[FeatureType.values().length];

        for (FeatureType feature : FeatureType.values()) {
            Integer trackIndex = feature.getTrackIndex();
            int idx = feature.ordinal();

            if (trackIndex != null) {
                features[idx] = track[trackIndex];
            } else {
                switch (feature) {
                    case SPEEDX:
                        features[idx] = speedX;
                        break;
                    case SPEEDY:
                        features[idx] = speedY;
                        break;
                    case ANGLE_TO_TRACK_AXIS:
                        features[idx] = angle;
                        break;
                    case TRACK_POSITION:
                        features[idx] = trackPos;
                        break;
                    default:
                        throw new IllegalArgumentException("Feature non riconosciuta: " + feature);
                }
            }
        }

        // Scrittura su file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            for (double v : features) {
                bw.write(String.format(Locale.ROOT, "%.4f;", v));
            }
            bw.write(classLabel + "\n");
        } catch (IOException e) {
            System.err.println("Errore scrittura CSV (raw): " + e.getMessage());
        }
    }


}
