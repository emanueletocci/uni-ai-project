package it.unisa.diem.ai.torcs.classifier;

import it.unisa.diem.ai.torcs.model.Sample;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

class KDTree {

    private final KDNode root;
    private int dimensions;

    public KDTree(List<Sample> points) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be empty");
        }
        // Ottengo il 1 sample e determino le sue dimensioni
        this.dimensions = points.get(0).getFeature().size();
        root = buildTree(points, 0);
    }

    private static class KDNode {
        Sample point;
        KDNode left, right;

        KDNode(Sample point) {
            this.point = point;
        }
    }

    private KDNode buildTree(List<Sample> points, int depth) {
        if (points.isEmpty()) {
            return null;
        }

        int axis = depth % dimensions;
        points.sort(Comparator.comparingDouble(p -> p.getFeature().get(axis)));
        int medianIndex = points.size() / 2;
        KDNode node = new KDNode(points.get(medianIndex));

        node.left = buildTree(points.subList(0, medianIndex), depth + 1);
        node.right = buildTree(points.subList(medianIndex + 1, points.size()), depth + 1);

        return node;
    }

    public List<Sample> kNearestNeighbors(Sample target, int k) {
        PriorityQueue<Sample> pq = new PriorityQueue<>(k, Comparator.comparingDouble(target::distanzaEuclidea).reversed());
        kNearestNeighbors(root, target, k, 0, pq);
        return new ArrayList<>(pq);
    }

    private void kNearestNeighbors(KDNode node, Sample target, int k, int depth, PriorityQueue<Sample> pq) {
        if (node == null) {
            return;
        }

        double distance = target.distanzaEuclidea(node.point);
        if (pq.size() < k) {
            pq.offer(node.point);
        } else if (distance < target.distanzaEuclidea(pq.peek())) {
            pq.poll();
            pq.offer(node.point);
        }

        int axis = depth % dimensions;
        KDNode nearNode = (target.getFeature().get(axis) < node.point.getFeature().get(axis)) ? node.left : node.right;
        KDNode farNode = (nearNode == node.left) ? node.right : node.left;

        kNearestNeighbors(nearNode, target, k, depth + 1, pq);

        if (pq.size() < k || Math.abs(target.getFeature().get(axis) - node.point.getFeature().get(axis)) < target.distanzaEuclidea(pq.peek())) {
            kNearestNeighbors(farNode, target, k, depth + 1, pq);
        }
    }
}
