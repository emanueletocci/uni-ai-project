package it.unisa.diem.ai.torcs.utils;

import it.unisa.diem.ai.torcs.model.FeatureVector;
import it.unisa.diem.ai.torcs.model.SensorFeature;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsabile della normalizzazione dei vettori di feature.
 * Ogni valore viene trasformato nel range [0,1] usando il metodo Min-Max,
 * in base ai valori minimi e massimi attesi per ciascuna feature.
 */
public class FeatureNormalizer {

    /** Valore minimo e massimo atteso per i sensori di bordo pista (track edge sensors). */
    public static final double MIN_TRACK_EDGE = 0.0;
    public static final double MAX_TRACK_EDGE = 200.0;

    /** Velocità longitudinale minima e massima dell’auto (speedX, in km/h). */
    public static final double MIN_SPEED_X = -100.0;
    public static final double MAX_SPEED_X = 300.0;

    /** Velocità laterale minima e massima dell’auto (speedY, in km/h). */
    public static final double MIN_SPEED_Y = -50.0;
    public static final double MAX_SPEED_Y = 50.0;

    /** Posizione laterale sulla pista (track position ∈ [-1, 1]). */
    public static final double MIN_TRACK_POSITION = -1.0;
    public static final double MAX_TRACK_POSITION = 1.0;

    /** Angolo rispetto all’asse pista (in radianti, range [-π, π]). */
    public static final double MIN_ANGLE_TO_TRACK_AXIS = -Math.PI;
    public static final double MAX_ANGLE_TO_TRACK_AXIS = Math.PI;

    /**
     * Applica la normalizzazione Min-Max al vettore di feature fornito,
     * restituendo un nuovo oggetto {@link FeatureVector} con valori nel range [0, 1].
     *
     * @param fv il vettore di feature da normalizzare
     * @return un nuovo {@link FeatureVector} con valori normalizzati
     */
    public FeatureVector normalize(FeatureVector fv) {
        List<Double> values = fv.getValues();
        List<Double> normalized = new ArrayList<>(values.size());
        SensorFeature[] features = SensorFeature.values();

        for (int i = 0; i < values.size(); i++) {
            double val = values.get(i);
            double min, max;

            // Determina il range di normalizzazione in base alla feature
            switch (features[i]) {
                case SPEED_X:
                    min = MIN_SPEED_X; max = MAX_SPEED_X;
                    break;
                case SPEED_Y:
                    min = MIN_SPEED_Y; max = MAX_SPEED_Y;
                    break;
                case ANGLE_TO_TRACK_AXIS:
                    min = MIN_ANGLE_TO_TRACK_AXIS; max = MAX_ANGLE_TO_TRACK_AXIS;
                    break;
                case TRACK_POSITION:
                    min = MIN_TRACK_POSITION; max = MAX_TRACK_POSITION;
                    break;
                default:
                    // Per i sensori di bordo pista
                    min = MIN_TRACK_EDGE; max = MAX_TRACK_EDGE;
                    break;
            }

            normalized.add(normalizzatoreMinMax(val, min, max));
        }

        return new FeatureVector(normalized);
    }

    /**
     * Funzione helper che applica la normalizzazione Min-Max a un singolo valore,
     * con clipping forzato nel range [0, 1].
     *
     * @param data valore da normalizzare
     * @param min valore minimo atteso
     * @param max valore massimo atteso
     * @return valore normalizzato nel range [0, 1]
     */
    private static double normalizzatoreMinMax(double data, double min, double max) {
        double normalized = (data - min) / (max - min);
        return Math.max(0.0, Math.min(1.0, normalized));  // clipping
    }
}
