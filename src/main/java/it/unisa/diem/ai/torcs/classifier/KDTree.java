package it.unisa.diem.ai.torcs.classifier;

import it.unisa.diem.ai.torcs.Sample;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Implementazione di un albero k-dimensionale (KD-Tree) per la ricerca dei K vicini più prossimi (KNN).
 */
class KDTree {

    private KDNode root;         // Nodo radice dell’albero
    private int dimensions;      // Numero di dimensioni (feature) per ogni Sample

    /**
     * Costruttore: costruisce l’albero a partire da una lista di punti.
     */
    public KDTree(List<Sample> points) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be empty");
        }

        // Ottiene il numero di dimensioni dal primo Sample
        this.dimensions = points.get(0).features.length;

        // Costruisce l'albero ricorsivamente
        root = buildTree(points, 0);
    }

    /**
     * Classe interna per rappresentare un nodo dell’albero.
     */
    private static class KDNode {
        Sample point;         // Il punto (Sample) contenuto nel nodo
        KDNode left, right;   // Sottorami sinistro e destro

        KDNode(Sample point) {
            this.point = point;
        }
    }

    /**
     * Costruisce ricorsivamente l’albero KD dividendo i dati lungo un asse ciclico.
     */
    private KDNode buildTree(List<Sample> points, int depth) {
        if (points.isEmpty()) {
            return null;
        }

        // Determina l’asse (dimensione) lungo cui dividere
        int axis = depth % dimensions;

        // Ordina i punti in base alla dimensione corrente
        points.sort(Comparator.comparingDouble(p -> p.features[axis]));

        // Trova il punto mediano
        int medianIndex = points.size() / 2;
        KDNode node = new KDNode(points.get(medianIndex));

        // Costruisce ricorsivamente il sottoalbero sinistro e destro
        node.left = buildTree(points.subList(0, medianIndex), depth + 1);
        node.right = buildTree(points.subList(medianIndex + 1, points.size()), depth + 1);

        return node;
    }

    /**
     * Restituisce i K vicini più prossimi a un dato punto target.
     */
    public List<Sample> kNearestNeighbors(Sample target, int k) {
        // Coda con priorità inversa: mantiene i vicini più prossimi ordinati per distanza decrescente
        PriorityQueue<Sample> pq = new PriorityQueue<>(k, Comparator.comparingDouble(target::distance).reversed());

        // Avvia la ricerca ricorsiva
        kNearestNeighbors(root, target, k, 0, pq);

        // Converte la coda in lista e la restituisce
        return new ArrayList<>(pq);
    }

    /**
     * Ricerca ricorsiva dei k vicini più prossimi in un KD-Tree.
     */
    private void kNearestNeighbors(KDNode node, Sample target, int k, int depth, PriorityQueue<Sample> pq) {
        if (node == null) {
            return;
        }

        // Calcola la distanza tra il nodo corrente e il target
        double distance = target.distance(node.point);

        // Se ci sono meno di k vicini, aggiungi questo
        if (pq.size() < k) {
            pq.offer(node.point);

            // Altrimenti, sostituisci il più lontano se quello attuale è più vicino
        } else if (distance < target.distance(pq.peek())) {
            pq.poll();
            pq.offer(node.point);
        }

        // Determina l’asse di divisione per questo livello
        int axis = depth % dimensions;

        // Scegli il ramo da visitare prima (quello vicino)
        KDNode nearNode = (target.features[axis] < node.point.features[axis]) ? node.left : node.right;
        KDNode farNode = (nearNode == node.left) ? node.right : node.left;

        // Visita il ramo vicino
        kNearestNeighbors(nearNode, target, k, depth + 1, pq);

        // Se necessario, esplora anche il ramo lontano
        if (pq.size() < k || Math.abs(target.features[axis] - node.point.features[axis]) < target.distance(pq.peek())) {
            kNearestNeighbors(farNode, target, k, depth + 1, pq);
        }
    }
}