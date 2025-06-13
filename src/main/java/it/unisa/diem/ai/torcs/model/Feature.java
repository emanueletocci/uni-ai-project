package it.unisa.diem.ai.torcs.model;

import java.util.ArrayList;
import java.util.List;

public class Feature {
    private List<Double> values;

    public Feature(List<Double> values) {
        this.values = new ArrayList<>(values);
    }

    public List<Double> getValues() {
        return new ArrayList<>(values);
    }

    public void setValues(List<Double> values) {
        this.values = new ArrayList<>(values);
    }

    public int size() {
        return values.size();
    }

    public Double get(int index) {
        return values.get(index);
    }

    public void set(int index, Double value) {
        values.set(index, value);
    }

    /**
     * Calcola la distanza euclidea tra questo oggetto Feature e un altro.
     */
    public double distanzaEuclidea(Feature altra) {
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

    @Override
    public String toString() {
        return values.toString();
    }
}
