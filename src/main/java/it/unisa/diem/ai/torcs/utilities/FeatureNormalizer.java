package it.unisa.diem.ai.torcs.utilities;

import it.unisa.diem.ai.torcs.model.FeatureType;

public class FeatureNormalizer {
    // Costanti di normalizzazione (devono essere coerenti con AutonomousDriverUtility)
    public static final double MAX_TRACK_VALUE = 200.0;
    public static final double MAX_SPEED = 310.0;
    public static final double MAX_TRACK_POSITION = 1.0;

    // Ottiene gli indici dei sensori dall'enum FeatureType
    public static final int[] SENSOR_INDICES = FeatureType.getTrackSensorIndices()
            .stream()
            .mapToInt(Integer::intValue)
            .toArray();

    private static double normalizzatoreMinMax(double data, double min, double max) {
        return (data - min) / (max - min);
    }

    // Clamp per evitare valori fuori dal range [0,1]
    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    // Normalizza un singolo valore di sensore di bordo pista
    private static double normalizeTrackSensor(double value) {
        // Se il valore è negativo (può capitare in caso di errore sensore), consideralo come 0
        return clamp01(normalizzatoreMinMax(Math.max(value, 0.0), 0.0, MAX_TRACK_VALUE));
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
        return clamp01(normalizzatoreMinMax(value, -MAX_TRACK_POSITION, MAX_TRACK_POSITION));
    }

    // Normalizza l'angolo rispetto all'asse della pista
    private static double normalizeAngleToTrackAxis(double value) {
        return clamp01(normalizzatoreMinMax(value, -Math.PI, Math.PI));
    }

    // Normalizza la velocità (ignora la retromarcia)
    private static double normalizeSpeed(double value) {
        return clamp01(normalizzatoreMinMax(Math.max(value, 0.0), 0.0, MAX_SPEED));
    }

    // Metodo di utilità per ottenere il vettore feature normalizzato (12 elementi)
    public static double[] extractAndNormalizeFeatures(double[] track, double trackPos, double angle, double speed) {
        double[] features = new double[FeatureType.values().length];
        double[] normalizedTrack = normalizeTrackSensors(track);
        System.arraycopy(normalizedTrack, 0, features, 0, normalizedTrack.length);
        features[7] = normalizeTrackPosition(trackPos);
        features[8] = normalizeAngleToTrackAxis(angle);
        features[9] = normalizeSpeed(speed);
        return features;
    }

    // Normalizzazione di un vettore preesistente di feature estratte
    public static double[] normalizeExtractedFeatures(double[] extractedFeatures) {
        // Si assume che extractedFeatures contenga già le feature nell'ordine:
        // [sensori_track_normalizzati (9 elementi), trackPos, angle, speed]
        if (extractedFeatures.length != FeatureType.values().length) {
            throw new IllegalArgumentException("Il vettore di feature deve contenere esattamente 12 elementi.");
        }

        double[] normalizedFeatures = new double[10];

        // Copia correttamente i sensori (9 elementi, da indice 0 a 6 incluso + i due eventuali extra se presenti)
        System.arraycopy(extractedFeatures, 0, normalizedFeatures, 0, 7);

        // Normalizza trackPos, angle e speed
        normalizedFeatures[7] = normalizeTrackPosition(extractedFeatures[7]);
        normalizedFeatures[8] = normalizeAngleToTrackAxis(extractedFeatures[8]);
        normalizedFeatures[9] = normalizeSpeed(extractedFeatures[9]);

        return normalizedFeatures;
    }
}
