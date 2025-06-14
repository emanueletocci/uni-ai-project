package it.unisa.diem.ai.torcs.utils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import it.unisa.diem.ai.torcs.model.*;

public class MultiHistogramFromSamples {

    public static class HistogramPanel extends JPanel {
        private final int binCount;
        private final String featureName;
        private final int[] frequencies;
        private final double min, max;

        public HistogramPanel(String featureName, List<Sample> samples, SensorFeature feature, int binCount) {
            this.featureName = featureName;
            this.binCount = binCount;
            this.frequencies = new int[binCount];

            List<Double> values = new ArrayList<>();
            for (Sample s : samples) {
                values.add(s.getFeature().getValues().get(feature.ordinal()));
            }

            this.min = values.stream().mapToDouble(d -> d).min().orElse(0);
            this.max = values.stream().mapToDouble(d -> d).max().orElse(1);
            double binWidth = (max - min) / binCount;

            for (double val : values) {
                int index = (int) ((val - min) / binWidth);
                if (index >= binCount) index = binCount - 1;
                frequencies[index]++;
            }

            setPreferredSize(new Dimension(400, 250));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth();
            int h = getHeight();
            int margin = 40;
            int maxFreq = Arrays.stream(frequencies).max().orElse(1);
            int barWidth = (w - 2 * margin) / binCount;

            g2.setColor(Color.BLACK);
            g2.drawLine(margin, h - margin, w - margin, h - margin);
            g2.drawLine(margin, margin, margin, h - margin);

            g2.drawString(featureName, w / 2 - g.getFontMetrics().stringWidth(featureName) / 2, margin - 10);

            for (int i = 0; i <= 5; i++) {
                int y = h - margin - i * (h - 2 * margin) / 5;
                int freq = maxFreq * i / 5;
                g2.drawString(String.valueOf(freq), 5, y + 5);
                g2.drawLine(margin - 5, y, margin, y);
            }

            double binWidthValue = (max - min) / binCount;
            for (int i = 0; i < binCount; i++) {
                int barHeight = (int) ((double) frequencies[i] / maxFreq * (h - 2 * margin));
                int x = margin + i * barWidth;
                int y = h - margin - barHeight;

                g2.setColor(Color.BLUE);
                g2.fillRect(x, y, barWidth - 2, barHeight);

                if (i % 2 == 0) {
                    String label = String.format("%.2f", min + i * binWidthValue);
                    int labelWidth = g.getFontMetrics().stringWidth(label);
                    g2.setColor(Color.BLACK);
                    g2.drawString(label, x + (barWidth - labelWidth) / 2, h - margin + 15);
                }

                // Quantitativi sopra la barra
                String valueStr = String.valueOf(frequencies[i]);
                int vw = g.getFontMetrics().stringWidth(valueStr);
                g2.setColor(Color.BLACK);
                g2.drawString(valueStr, x + (barWidth - vw) / 2, y - 5);
            }
        }
    }

    public static List<Sample> loadSamples(String path) {
        return Dataset.loadFromCSV(path).getSamples();
    }

    public static void main(String[] args) {
        String rawPath = "data/driver_dataset.csv";
        String normPath = "data/driver_dataset_balanced.csv";

        List<Sample> rawSamples = loadSamples(rawPath);
        List<Sample> normSamples = loadSamples(normPath);

        JFrame frame = new JFrame("Confronto distribuzioni RAW vs NORMALIZZATO");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel grid = new JPanel(new GridLayout(SensorFeature.values().length + 1, 2, 10, 10));

        for (SensorFeature feature : SensorFeature.values()) {
            grid.add(new HistogramPanel("RAW - " + feature.name(), rawSamples, feature, 20));
            grid.add(new HistogramPanel("NORM - " + feature.name(), normSamples, feature, 20));
        }

        // Label distribution panel (bottom row)
        grid.add(new LabelDistributionPanel(rawSamples, "RAW - LABEL"));
        grid.add(new LabelDistributionPanel(normSamples, "NORM - LABEL"));

        JScrollPane scroll = new JScrollPane(grid);
        frame.setContentPane(scroll);
        frame.setSize(1100, 1000);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static class LabelDistributionPanel extends JPanel {
        private final Map<it.unisa.diem.ai.torcs.model.Label, Integer> labelCounts;
        private final String title;

        public LabelDistributionPanel(List<Sample> samples, String title) {
            this.title = title;
            labelCounts = new LinkedHashMap<>();
            for (it.unisa.diem.ai.torcs.model.Label l : it.unisa.diem.ai.torcs.model.Label.values()) {
                labelCounts.put(l, 0);
            }
            for (Sample s : samples) {
                it.unisa.diem.ai.torcs.model.Label lbl = s.getLabel();
                labelCounts.put(lbl, labelCounts.get(lbl) + 1);
            }
            setPreferredSize(new Dimension(500, 200));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth();
            int h = getHeight();
            int margin = 50;
            int labelCount = labelCounts.size();
            int maxCount = Collections.max(labelCounts.values());
            int barWidth = (w - 2 * margin) / labelCount;

            g2.setColor(Color.BLACK);
            g2.drawLine(margin, h - margin, w - margin, h - margin);
            g2.drawLine(margin, margin, margin, h - margin);
            g2.drawString(title, w / 2 - g.getFontMetrics().stringWidth(title) / 2, margin - 15);

            int i = 0;
            for (Map.Entry<it.unisa.diem.ai.torcs.model.Label, Integer> entry : labelCounts.entrySet()) {
                int x = margin + i * barWidth;
                int barHeight = (int) ((double) entry.getValue() / maxCount * (h - 2 * margin));
                int y = h - margin - barHeight;

                g2.setColor(Color.ORANGE);
                g2.fillRect(x, y, barWidth - 4, barHeight);

                g2.setColor(Color.BLACK);
                String labelStr = entry.getKey().toString();
                int lw = g.getFontMetrics().stringWidth(labelStr);
                g2.drawString(labelStr, x + (barWidth - lw) / 2, h - margin + 15);

                // Quantitativi sopra la barra
                String valueStr = String.valueOf(entry.getValue());
                int vw = g.getFontMetrics().stringWidth(valueStr);
                g2.drawString(valueStr, x + (barWidth - vw) / 2, y - 5);

                i++;
            }
        }
    }
}