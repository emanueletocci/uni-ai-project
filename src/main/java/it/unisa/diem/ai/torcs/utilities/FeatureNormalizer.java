package it.unisa.diem.ai.torcs.utilities;

import it.unisa.diem.ai.torcs.model.FeatureType;

/**
 * Classe di utilità per la normalizzazione delle feature utilizzate nel sistema di guida autonoma.
 * Le feature includono sensori di bordo pista, posizione sulla pista, angolo rispetto all'asse e velocità.
 * <p>
 * Le normalizzazioni sono coerenti con i valori attesi dal modello di apprendimento automatico.
 */
public class FeatureNormalizer {

    /** Valore massimo atteso per i sensori di bordo pista. */
    public static final double MAX_TRACK_VALUE = 200.0;

    /** Velocità massima dell’auto, usata per la normalizzazione. */
    public static final double MAX_SPEED = 290.0;

    /** Massima deviazione laterale sulla pista (track position ∈ [-2, 2]). */
    public static final double MAX_TRACK_POSITION = 2.0;

    /** Indici dei sensori di bordo pista usati (in base all'enum FeatureType). */
    public static final int[] SENSOR_INDICES = FeatureType.getTrackSensorIndices()
            .stream()
            .mapToInt(Integer::intValue)
            .toArray();

    /**
     * Normalizza un valore usando la tecnica Min-Max.
     *
     * @param data Valore da normalizzare.
     * @param min  Minimo del dominio.
     * @param max  Massimo del dominio.
     * @return Valore normalizzato in [0,1].
     */
    private static double normalizzatoreMinMax(double data, double min, double max) {
        return (data - min) / (max - min);
    }

    /**
     * Normalizza un singolo valore di sensore di bordo pista.
     *
     * @param value Valore del sensore.
     * @return Valore normalizzato.
     */
    private static double normalizeTrackSensor(double value) {
        return normalizzatoreMinMax(value, -1.0, MAX_TRACK_VALUE);
    }

    /**
     * Normalizza tutti i sensori di bordo pista specificati nell'enum FeatureType.
     *
     * @param track Vettore completo dei 19 sensori di bordo pista.
     * @return Vettore dei sensori selezionati e normalizzati.
     */
    private static double[] normalizeTrackSensors(double[] track) {
        double[] normalized = new double[SENSOR_INDICES.length];
        for (int i = 0; i < SENSOR_INDICES.length; i++) {
            normalized[i] = normalizeTrackSensor(track[SENSOR_INDICES[i]]);
        }
        return normalized;
    }

    /**
     * Normalizza la posizione del veicolo sulla pista.
     *
     * @param value Track position ∈ [-MAX_TRACK_POSITION, MAX_TRACK_POSITION].
     * @return Valore normalizzato ∈ [0,1].
     */
    private static double normalizeTrackPosition(double value) {
        return normalizzatoreMinMax(value, -MAX_TRACK_POSITION, MAX_TRACK_POSITION);
    }

    /**
     * Normalizza l'angolo tra l'asse del veicolo e quello della pista.
     *
     * @param value Angolo ∈ [-π, π].
     * @return Valore normalizzato ∈ [0,1].
     */
    private static double normalizeAngleToTrackAxis(double value) {
        return normalizzatoreMinMax(value, -Math.PI, Math.PI);
    }

    /**
     * Normalizza la velocità del veicolo.
     *
     * @param value Velocità in km/h.
     * @return Valore normalizzato ∈ [0,1].
     */
    private static double normalizeSpeed(double value) {
        return normalizzatoreMinMax(value, 0.0, MAX_SPEED);
    }

    /**
     * Estrae e normalizza le feature per il modello, mantenendo l'ordine stabilito da {@link FeatureType}.
     * I valori normalizzati includono sensori di bordo pista, track position, angolo e velocità.
     *
     * @param track    Vettore dei 19 sensori di bordo pista.
     * @param trackPos Posizione sulla pista.
     * @param angle    Angolo rispetto all'asse della pista.
     * @param speed    Velocità dell'auto.
     * @return Vettore di feature normalizzate nell’ordine definito da FeatureType.
     */
    public static double[] extractAndNormalizeFeatures(double[] track, double trackPos, double angle, double speed) {
        double[] features = new double[FeatureType.values().length];

        for (FeatureType feature : FeatureType.values()) {
            Integer trackIndex = feature.getTrackIndex();
            int idx = feature.ordinal();

            if (trackIndex != null) {
                // Feature sensore: normalizza il sensore corrispondente dal vettore track
                features[idx] = normalizeTrackSensor(track[trackIndex]);
            } else {
                // Feature non sensoriale: normalizza in base al tipo
                switch (feature) {
                    case SPEED:
                        features[idx] = normalizeSpeed(speed);
                        break;
                    case ANGLE_TO_TRACK_AXIS:
                        features[idx] = normalizeAngleToTrackAxis(angle);
                        break;
                    case TRACK_POSITION:
                        features[idx] = normalizeTrackPosition(trackPos);
                        break;
                    default:
                        throw new IllegalArgumentException("Feature non riconosciuta: " + feature);
                }
            }
        }

        return features;
    }

    /**
     * Normalizza un vettore di feature già estratte (es. da file o memoria), utilizzando la struttura definita da {@link FeatureType}.
     * Utile quando si importano dati grezzi da normalizzare in un secondo momento.
     *
     * @param extractedFeatures Vettore di feature da normalizzare.
     * @param track             Vettore dei 19 sensori di bordo pista (necessario per reinserire i valori reali dei sensori).
     * @return Vettore di feature normalizzate.
     */
    public static double[] normalizeExtractedFeatures(double[] extractedFeatures, double[] track) {
        if (extractedFeatures.length != FeatureType.values().length) {
            throw new IllegalArgumentException("Il vettore di feature deve contenere esattamente " + FeatureType.values().length + " elementi.");
        }

        double[] normalized = new double[extractedFeatures.length];

        for (FeatureType feature : FeatureType.values()) {
            int idx = feature.ordinal();
            Integer trackIndex = feature.getTrackIndex();

            if (trackIndex != null) {
                normalized[idx] = normalizeTrackSensor(track[trackIndex]);
            } else {
                switch (feature) {
                    case SPEED:
                        normalized[idx] = normalizeSpeed(extractedFeatures[idx]);
                        break;
                    case ANGLE_TO_TRACK_AXIS:
                        normalized[idx] = normalizeAngleToTrackAxis(extractedFeatures[idx]);
                        break;
                    case TRACK_POSITION:
                        normalized[idx] = normalizeTrackPosition(extractedFeatures[idx]);
                        break;
                    default:
                        throw new IllegalArgumentException("Feature non riconosciuta: " + feature);
                }
            }
        }

        return normalized;
    }
}
