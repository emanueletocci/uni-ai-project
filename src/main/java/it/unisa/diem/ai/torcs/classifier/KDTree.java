package it.unisa.diem.ai.torcs.classifier;

import it.unisa.diem.ai.torcs.model.Sample;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Implementazione di un KD-Tree per la ricerca dei k-nearest neighbors
 * su un insieme di punti rappresentati da oggetti {@link Sample}.
 * <p>
 * Il KD-Tree consente ricerche efficienti in spazi multidimensionali.
 */
class KDTree {

    /** Nodo radice dell'albero KD */
    private final KDNode root;

    /** Numero di dimensioni dei punti (feature) */
    private int dimensions;

    /**
     * Costruttore che costruisce un KD-Tree a partire da una lista di Sample.
     *
     * @param points lista dei punti su cui costruire il KD-Tree
     * @throws IllegalArgumentException se la lista è vuota
     */
    public KDTree(List<Sample> points) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be empty");
        }
        // Determina il numero di dimensioni da un campione
        this.dimensions = points.getFirst().getFeature().size();
        root = buildTree(points, 0);
    }

    /**
     * Classe interna che rappresenta un nodo del KD-Tree.
     */
    private static class KDNode {
        Sample point;
        KDNode left, right;

        KDNode(Sample point) {
            this.point = point;
        }
    }

    /**
     * Metodo ricorsivo per costruire l'albero KD.
     *
     * @param points lista di punti da cui costruire il sottoalbero
     * @param depth profondità attuale dell'albero (serve per scegliere l'asse)
     * @return il nodo radice del sottoalbero
     */
    private KDNode buildTree(List<Sample> points, int depth) {
        if (points.isEmpty()) {
            return null;
        }

        // Seleziona l'asse su cui effettuare lo split (ciclico)
        int axis = depth % dimensions;

        // Ordina i punti secondo la coordinata dell'asse corrente
        points.sort(Comparator.comparingDouble(p -> p.getFeature().get(axis)));

        // Trova il punto mediano
        int medianIndex = points.size() / 2;
        KDNode node = new KDNode(points.get(medianIndex));

        // Ricorsione per i sottoalberi sinistro e destro
        node.left = buildTree(points.subList(0, medianIndex), depth + 1);
        node.right = buildTree(points.subList(medianIndex + 1, points.size()), depth + 1);

        return node;
    }

    /**
     * Restituisce i k-nearest neighbors del target all'interno dell'albero.
     *
     * @param target punto target da cui calcolare le distanze
     * @param k numero di vicini da trovare
     * @return lista dei k campioni più vicini
     */
    public List<Sample> kNearestNeighbors(Sample target, int k) {
        // Coda con massimo k elementi, ordinati per distanza decrescente
        PriorityQueue<Sample> pq = new PriorityQueue<>(k, Comparator.comparingDouble(target::distanzaEuclidea).reversed());
        kNearestNeighbors(root, target, k, 0, pq);
        return new ArrayList<>(pq);
    }

    /**
     * Metodo ricorsivo che attraversa il KD-Tree per trovare i k-nearest neighbors.
     *
     * @param node nodo corrente dell'albero
     * @param target punto target
     * @param k numero di vicini desiderati
     * @param depth profondità attuale dell'albero
     * @param pq coda prioritaria che mantiene i k vicini migliori trovati
     */
    private void kNearestNeighbors(KDNode node, Sample target, int k, int depth, PriorityQueue<Sample> pq) {
        if (node == null) {
            return;
        }

        double distance = target.distanzaEuclidea(node.point);

        // Se la coda non è piena o si trova un punto più vicino, aggiorna la coda
        if (pq.size() < k) {
            pq.offer(node.point);
        } else if (distance < target.distanzaEuclidea(pq.peek())) {
            pq.poll();
            pq.offer(node.point);
        }

        int axis = depth % dimensions;

        // Decidi se andare a sinistra o destra nell'albero
        KDNode nearNode = (target.getFeature().get(axis) < node.point.getFeature().get(axis)) ? node.left : node.right;
        KDNode farNode = (nearNode == node.left) ? node.right : node.left;

        // Esplora prima il sottoalbero più vicino
        kNearestNeighbors(nearNode, target, k, depth + 1, pq);

        // Verifica se è necessario esplorare anche l'altro ramo
        if (pq.size() < k || Math.abs(target.getFeature().get(axis) - node.point.getFeature().get(axis)) < target.distanzaEuclidea(pq.peek())) {
            kNearestNeighbors(farNode, target, k, depth + 1, pq);
        }
    }
}
