package it.unisa.diem.ai.torcs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta un vettore di feature numeriche (valori Double),
 * utilizzato per descrivere un campione (Sample) nel contesto dell'apprendimento automatico.
 * Supporta accesso, modifica, e calcolo della distanza euclidea tra vettori.
 */
public class FeatureVector {

    /** Lista di valori numerici che compongono il vettore di feature */
    private List<Double> values;

    /**
     * Costruttore che crea un FeatureVector copiando una lista di Double.
     *
     * @param values lista di valori da usare come feature
     */
    public FeatureVector(List<Double> values) {
        this.values = new ArrayList<>(values); // Copia difensiva
    }

    /**
     * Restituisce una copia della lista di valori del vettore.
     *
     * @return lista dei valori
     */
    public List<Double> getValues() {
        return new ArrayList<>(values); // Copia difensiva per evitare modifiche esterne
    }

    /**
     * Imposta i valori del vettore, sostituendo quelli esistenti.
     *
     * @param values nuova lista di valori
     */
    public void setValues(List<Double> values) {
        this.values = new ArrayList<>(values);
    }

    /**
     * Restituisce la dimensione del vettore.
     *
     * @return numero di feature
     */
    public int size() {
        return values.size();
    }

    /**
     * Restituisce il valore alla posizione indicata.
     *
     * @param index posizione nel vettore
     * @return valore Double corrispondente
     */
    public Double get(int index) {
        return values.get(index);
    }

    /**
     * Imposta un valore in una determinata posizione.
     *
     * @param index indice della feature da modificare
     * @param value nuovo valore da assegnare
     */
    public void set(int index, Double value) {
        values.set(index, value);
    }

    /**
     * Calcola la distanza euclidea tra questo vettore e un altro.
     *
     * @param altra altro vettore da confrontare
     * @return distanza euclidea tra i due vettori
     * @throws IllegalArgumentException se i vettori hanno dimensioni diverse
     */
    public double distanzaEuclidea(FeatureVector altra) {
        if (this.values.size() != altra.values.size()) {
            throw new IllegalArgumentException("I vettori delle feature devono avere la stessa dimensione");
        }
        double somma = 0.0;
        for (int i = 0; i < values.size(); i++) {
            double diff = this.values.get(i) - altra.values.get(i);
            somma += diff * diff;
        }
        return Math.sqrt(somma);
    }

    /**
     * Restituisce una rappresentazione testuale del vettore.
     *
     * @return stringa con i valori del vettore
     */
    @Override
    public String toString() {
        return values.toString();
    }
}
