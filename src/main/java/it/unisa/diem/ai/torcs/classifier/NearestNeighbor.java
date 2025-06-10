package it.unisa.diem.ai.torcs.classifier;

import it.unisa.diem.ai.torcs.Sample;

import java.io.*;
import java.util.*;

/**
 * Classificatore basato su K-Nearest Neighbors (KNN) che utilizza un KDTree
 * per velocizzare la ricerca dei vicini più prossimi.
 */
public class NearestNeighbor {

    private final List<Sample> trainingData;  // Lista dei dati di addestramento
    private KDTree kdtree;                    // Albero KD per la ricerca rapida
    private final int[] classCounts;          // Array per contare le occorrenze di ciascuna classe (0–9)

    /**
     * Costruttore: inizializza e carica i dati dal file CSV.
     * @param filename Percorso del file CSV con i dati di training
     */
    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.kdtree = null;
        this.classCounts = new int[8];  // Suppone classi etichettate da 0 a 9
        this.readPointsFromCSV(filename);
    }

    /**
     * Legge i dati da un file CSV e costruisce la struttura KDTree.
     */
    private void readPointsFromCSV(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Legge e scarta la prima riga (header)
            String header = reader.readLine();
            if (header == null) {
                System.err.println("File vuoto!");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Ignora eventuali header ripetuti (in caso di merge di file)
                if (line.toLowerCase().contains("track0") || line.toLowerCase().contains("classlabel")) {
                    System.err.println("Salto header ripetuto o riga non dati: " + line);
                    continue;
                }

                // Assume che il CSV sia separato da ; e abbia 13 colonne (12 feature + 1 classe)
                String[] parts = line.split(";");
                if (parts.length != 13) {
                    System.err.println("Riga malformata: " + line);
                    continue;
                }

                try {
                    trainingData.add(new Sample(line));  // Costruisce un oggetto Sample da una riga
                } catch (NumberFormatException e) {
                    System.err.println("Errore di parsing nella riga: " + line);
                }
            }

            if (trainingData.isEmpty()) {
                System.err.println("Nessun dato nel file dopo l'header!");
            }

            // Costruisce il KDTree con i dati validi
            this.kdtree = new KDTree(trainingData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Trova i K vicini più prossimi a un punto dato.
     */
    public List<Sample> findKNearestNeighbors(Sample testPoint, int k) {
        return kdtree.kNearestNeighbors(testPoint, k);
    }

    /**
     * Classifica un punto test in base alla maggioranza dei K vicini.
     */
    public int classify(Sample testPoint, int k) {
        List<Sample> kNearestNeighbors = findKNearestNeighbors(testPoint, k);

        // Azzera il conteggio delle classi
        Arrays.fill(classCounts, 0);

        // Conta quante volte compare ogni classe nei vicini
        for (Sample neighbor : kNearestNeighbors) {
            classCounts[neighbor.cls]++;
        }

        // Trova la classe più frequente (maggioranza)
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
     * Restituisce i dati di training (utile per analisi/debug).
     */
    public List<Sample> getTrainingData() {
        return trainingData;
    }
}