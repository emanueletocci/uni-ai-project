package it.unisa.diem.ai.torcs.utilities;

public class FeatureNormalizer {
    // Costanti di normalizzazione (devono essere coerenti con AutonomousDriverUtility)
    public static final double MAX_TRACK_VALUE = 200.0;
    public static final double MAX_SPEED = 290.0;
    public static final double MAX_TRACK_POSITION = 2.0;
    public static final int[] SENSOR_INDICES = {0, 3, 4, 8, 9, 10, 14, 15, 18};
    public static final double STEER_LIMIT = 0.8;

    private static double normalizzatoreMinMax(double data, double min, double max) {
        return (data - min) / (max - min);
    }

    // Normalizza un singolo valore di sensore di bordo pista
    private static double normalizeTrackSensor(double value) {
        return normalizzatoreMinMax(value, -1.0, MAX_TRACK_VALUE);
    }

    // Normalizza tutti i sensori di bordo pista selezionati
    private static double[] normalizeTrackSensors(double[] track) {
        double[] normalized = new double[SENSOR_INDICES.length];
        for (int i = 0; i < SENSOR_INDICES.length; i++) {
            normalized[i] = normalizeTrackSensor(track[SENSOR_INDICES[i]]);
        }
        return normalized;
    }

    // Normalizza la posizione sulla pista (già in [-1,1])
    private static double normalizeTrackPosition(double value) {
        return normalizzatoreMinMax(value, -MAX_TRACK_POSITION, MAX_TRACK_POSITION);
    }

    // Normalizza l'angolo rispetto all'asse della pista
    private static double normalizeAngleToTrackAxis(double value) {
        return normalizzatoreMinMax(value, -Math.PI, Math.PI);
    }

    // Normalizza la velocità
    private static double normalizeSpeed(double value) {
        return normalizzatoreMinMax(value, 0.0, MAX_SPEED);
    }

    // Metodo di utilità per ottenere il vettore feature normalizzato (12 elementi)
    public static double[] extractAndNormalizeFeatures(double[] track, double trackPos, double angle, double speed) {
        double[] features = new double[12];
        double[] normalizedTrack = normalizeTrackSensors(track);
        System.arraycopy(normalizedTrack, 0, features, 0, normalizedTrack.length);
        features[9] = normalizeTrackPosition(trackPos);
        features[10] = normalizeAngleToTrackAxis(angle);
        features[11] = normalizeSpeed(speed);
        return features;
    }


    // Normalizzazione di un vettore preesistente di feature estratte
    public static double[] normalizeExtractedFeatures(double[] extractedFeatures) {
        // Si assume che extractedFeatures contenga già le feature nell'ordine:
        // [sensori_track_normalizzati (9 elementi), trackPos, angle, speed]
        if (extractedFeatures.length != 12) {
            throw new IllegalArgumentException("Il vettore di feature deve contenere esattamente 12 elementi.");
        }

        double[] normalizedFeatures = new double[12];

        System.arraycopy(extractedFeatures, 0, normalizedFeatures, 0, 9);

        // Normalizza gli ultimi 3 elementi
        normalizedFeatures[9] = normalizeTrackPosition(extractedFeatures[9]);
        normalizedFeatures[10] = normalizeAngleToTrackAxis(extractedFeatures[10]);
        normalizedFeatures[11] = normalizeSpeed(extractedFeatures[11]);

        return normalizedFeatures;
    }
}