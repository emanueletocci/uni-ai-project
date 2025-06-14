package it.unisa.diem.ai.torcs.classifier;

import java.util.Arrays;
import java.util.List;

import it.unisa.diem.ai.torcs.model.Dataset;
import it.unisa.diem.ai.torcs.model.Label;
import it.unisa.diem.ai.torcs.model.Sample;

/**
 * Implementazione semplice dell'algoritmo Nearest Neighbor (KNN)
 * utilizzando un KD-Tree per una ricerca efficiente dei vicini.
 */
public class NearestNeighbor {

    /** Dataset di addestramento */
    private final Dataset trainingData;

    /** Struttura KD-Tree costruita a partire dal dataset */
    private final KDTree kdtree;

    /** Contatore per le classi durante la classificazione */
    private final int[] classCounts;

    /**
     * Costruttore che inizializza il classificatore con il dataset di addestramento.
     *
     * @param trainingData dataset contenente i campioni noti (labeled)
     */
    public NearestNeighbor(Dataset trainingData) {
        this.trainingData = trainingData;
        this.kdtree = new KDTree(trainingData.getSamples());
        this.classCounts = new int[Label.values().length];
    }

    /**
     * Trova i k vicini più prossimi rispetto a un punto di test.
     *
     * @param testPoint il punto da classificare
     * @param k numero di vicini da considerare
     * @return lista dei k vicini più vicini
     */
    public List<Sample> findKNearestNeighbors(Sample testPoint, int k) {
        return kdtree.kNearestNeighbors(testPoint, k);
    }

    /**
     * Classifica un punto di test usando la maggioranza tra i k vicini più prossimi.
     *
     * @param testPoint il punto da classificare
     * @param k numero di vicini da considerare
     * @return codice numerico della classe predetta (int corrispondente a {@link Label#getCode()})
     */
    public int classify(Sample testPoint, int k) {
        List<Sample> kNearestNeighbors = findKNearestNeighbors(testPoint, k);

        // Azzeramento dei conteggi
        Arrays.fill(classCounts, 0);

        // Conta le occorrenze di ciascuna classe tra i vicini
        for (Sample neighbor : kNearestNeighbors) {
            int classCode = neighbor.getLabel().getCode();
            classCounts[classCode]++;
        }

        // Seleziona la classe con il maggior numero di occorrenze
        int maxCount = -1;
        int predictedClass = -1;
        for (int i = 0; i < classCounts.length; i++) {
            if (classCounts[i] > maxCount) {
                maxCount = classCounts[i];
                predictedClass = i;
            }
        }

        return predictedClass;
    }

    /**
     * Restituisce il dataset di addestramento usato dal classificatore.
     *
     * @return il dataset con i campioni etichettati
     */
    public Dataset getTrainingData() {
        return trainingData;
    }
}
