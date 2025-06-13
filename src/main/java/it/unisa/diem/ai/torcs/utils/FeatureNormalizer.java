package it.unisa.diem.ai.torcs.utils;

import it.unisa.diem.ai.torcs.model.FeatureVector;
import it.unisa.diem.ai.torcs.model.SensorFeature;
import java.util.ArrayList;
import java.util.List;

public class FeatureNormalizer {

    /** Costanti (modifica se necessario) */
    /** Valore minimo e massimo atteso per i sensori di bordo pista (track edge sensors). */
    public static final double MIN_TRACK_EDGE = 0.0;
    public static final double MAX_TRACK_EDGE = 200.0;

    /** Velocità longitudinale minima e massima dell’auto (speedX, in km/h). */
    public static final double MIN_SPEED_X = 0.0;
    public static final double MAX_SPEED_X = 290.0;

    /** Velocità laterale minima e massima dell’auto (speedY, in km/h). */
    public static final double MIN_SPEED_Y = -40.0;
    public static final double MAX_SPEED_Y = 40.0;

    /** Velocità massima in retromarcia (in modulo, km/h). */
    public static final double MIN_NEGATIVE_SPEED = 0.001;
    public static final double MAX_NEGATIVE_SPEED = 60.0;

    /** Posizione laterale massima sulla pista (track position ∈ [-1, 1]). */
    public static final double MIN_TRACK_POSITION = -1.0;
    public static final double MAX_TRACK_POSITION = 1.0;

    /** Angolo massimo rispetto all’asse pista (in radianti, [-π, π]). */
    public static final double MIN_ANGLE_TO_TRACK_AXIS = -Math.PI;
    public static final double MAX_ANGLE_TO_TRACK_AXIS = Math.PI;

    /**
     * Normalizza un FeatureVector usando i range predefiniti.
     */
    public FeatureVector normalize(FeatureVector fv) {
        List<Double> values = fv.getValues();
        List<Double> normalized = new ArrayList<>(values.size());
        SensorFeature[] features = SensorFeature.values();

        for (int i = 0; i < values.size(); i++) {
            double val = values.get(i);
            double min, max;

            // Scegli il range in base alla feature
            switch (features[i]) {
                case SPEED_X:
                    min = MIN_SPEED_X; max = MAX_SPEED_X; break;
                case SPEED_Y:
                    min = MIN_SPEED_Y; max = MAX_SPEED_Y; break;
                case ANGLE_TO_TRACK_AXIS:
                    min = MIN_ANGLE_TO_TRACK_AXIS; max = MAX_ANGLE_TO_TRACK_AXIS; break;
                case TRACK_POSITION:
                    min = MIN_TRACK_POSITION; max = MAX_TRACK_POSITION; break;
                default:
                    // Tutti i sensori di bordo pista
                    min = MIN_TRACK_EDGE; max = MAX_TRACK_EDGE; break;
            }
            normalized.add(normalizzatoreMinMax(val, min, max));
        }
        return new FeatureVector(normalized);
    }

    /**
     * Normalizzatore min-max con clipping tra 0 e 1.
     */
    private static double normalizzatoreMinMax(double data, double min, double max) {
        if (max == min) return 0.0;
        double norm = (data - min) / (max - min);
        return Math.max(0.0, Math.min(1.0, norm));
    }
}
