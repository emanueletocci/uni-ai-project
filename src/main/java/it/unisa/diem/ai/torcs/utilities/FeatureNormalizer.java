package it.unisa.diem.ai.torcs.utilities;

import it.unisa.diem.ai.torcs.model.FeatureType;

/**
 * Classe di utilità per la normalizzazione delle feature utilizzate nel sistema di guida autonoma TORCS.
 * Le feature includono sensori di bordo pista, posizione laterale, angolo rispetto all’asse pista e velocità.
 * <p>
 * Tutte le normalizzazioni restituiscono valori nell’intervallo [0, 1], a meno che non sia diversamente specificato.
 * I range di input sono definiti secondo la documentazione ufficiale TORCS e le specifiche di progetto.
 */
public class FeatureNormalizer {

    // --- COSTANTI DI NORMALIZZAZIONE ---

    /** Valore minimo e massimo atteso per i sensori di bordo pista (track edge sensors). */
    public static final double MIN_TRACK_VALUE = 0.0;
    public static final double MAX_TRACK_VALUE = 200.0;

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

    // --- METODI DI NORMALIZZAZIONE ---

    /**
     * Normalizza un valore usando la tecnica Min-Max.
     *
     * @param data Valore da normalizzare.
     * @param min  Minimo del dominio.
     * @param max  Massimo del dominio.
     * @return Valore normalizzato in [0, 1].
     */
    private static double normalizzatoreMinMax(double data, Double min, Double max) {
        if (max == min) return 0.0; // evita divisione per 0
        double norm = (data - min) / (max - min);
        return Math.max(0.0, Math.min(1.0, norm)); // clipping
    }

    /**
     * Normalizza un singolo valore di sensore di bordo pista.
     *
     * @param value Valore del sensore (input atteso: [0, 200]).
     * @return Valore normalizzato in [0, 1].
     */
    private static double normalizeTrackSensor(double value) {
        return normalizzatoreMinMax(value, MIN_TRACK_VALUE, MAX_TRACK_VALUE);
    }

    /**
     * Normalizza la posizione del veicolo sulla pista.
     *
     * @param value Track position (input atteso: [-1, 1]; valori fuori range vengono "clippati").
     * @return Valore normalizzato in [0, 1].
     */
    private static double normalizeTrackPosition(double value) {
        return normalizzatoreMinMax(value, MIN_TRACK_POSITION, MAX_TRACK_POSITION);
    }

    /**
     * Normalizza l'angolo tra l'asse del veicolo e quello della pista.
     *
     * @param value Angolo in radianti (input atteso: [-π, π]).
     * @return Valore normalizzato in [0, 1].
     */
    private static double normalizeAngleToTrackAxis(double value) {
        return normalizzatoreMinMax(value, MIN_ANGLE_TO_TRACK_AXIS, MAX_ANGLE_TO_TRACK_AXIS);
    }

    /**
     * Normalizza la velocità longitudinale del veicolo.
     *
     * @param value Velocità in km/h (input atteso: [0, 290]).
     * @return Valore normalizzato in [0, 1].
     */
    private static double normalizeSpeedX(double value) {
        return normalizzatoreMinMax(value, MIN_SPEED_X, MAX_SPEED_X);
    }

    /**
     * Normalizza la velocità in retromarcia (valori negativi), usando il modulo.
     *
     * @param value Velocità negativa in km/h (input atteso: [-60, -0.001]).
     * @return Valore normalizzato in [0, 1].
     */
    private static double normalizeNegativeSpeed(double value) {
        return normalizzatoreMinMax(-value, MIN_NEGATIVE_SPEED, MAX_NEGATIVE_SPEED);
    }

    /**
     * Normalizza la velocità laterale del veicolo.
     *
     * @param value Velocità laterale in km/h (input atteso: [-40, 40]).
     * @return Valore normalizzato in [0, 1].
     */
    private static double normalizeSpeedY(double value) {
        return normalizzatoreMinMax(value, MIN_SPEED_Y, MAX_SPEED_Y);
    }

    /**
     * Estrae e normalizza le feature per il modello, mantenendo l'ordine stabilito da {@link FeatureType}.
     * I valori normalizzati includono sensori di bordo pista, track position, angolo e velocità.
     *
     * @param track    Vettore dei 19 sensori di bordo pista (ciascun valore atteso in [0, 200]).
     * @param trackPos Posizione sulla pista (atteso in [-1, 1]).
     * @param angle    Angolo rispetto all'asse della pista (atteso in [-π, π]).
     * @param speedX   Velocità longitudinale (attesa in [0, 290]).
     * @param speedY   Velocità laterale (attesa in [-40, 40]).
     * @return Vettore di feature normalizzate in [0, 1].
     */
    public static double[] extractAndNormalizeFeatures(double[] track, double trackPos, double angle, double speedX, double speedY) {
        double[] features = new double[FeatureType.values().length];

        for (FeatureType feature : FeatureType.values()) {
            Integer trackIndex = feature.getTrackIndex();
            int idx = feature.ordinal();

            if (trackIndex != null) {
                features[idx] = normalizeTrackSensor(track[trackIndex]);
            } else {
                switch (feature) {
                    case SPEEDX:
                        features[idx] = (speedX >= 0) ? normalizeSpeedX(speedX) : normalizeNegativeSpeed(speedX);
                        break;
                    case SPEEDY:
                        features[idx] = normalizeSpeedY(speedY);
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
     * @return Vettore di feature normalizzate in [0, 1].
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
                    case SPEEDX:
                        normalized[idx] = (extractedFeatures[idx] >= 0)
                                ? normalizeSpeedX(extractedFeatures[idx])
                                : normalizeNegativeSpeed(extractedFeatures[idx]);
                        break;
                    case SPEEDY:
                        normalized[idx] = normalizeSpeedY(extractedFeatures[idx]);
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
