package it.unisa.diem.ai.torcs.utils;

import it.unisa.diem.ai.torcs.model.SensorFeature;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RadarVisualizer extends JPanel {
    private double[] distances = new double[19];

    public void updateSensors(double[] newDistances) {
        this.distances = newDistances;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        // Punto di partenza in basso al centro
        int originX = width / 2;
        int originY = height - 20;

        // Scala automatica (massimo 100 metri reali)
        double maxLength = 100.0;
        double scale = (height - 40) / maxLength;

        // Ottieni solo gli indici dei sensori da visualizzare
        List<Integer> indices = SensorFeature.getTrackSensorIndices();

        for (int i : indices) {
            double angleDeg = -90 + i * 10;
            double angleRad = Math.toRadians(angleDeg);
            double length = Math.min(distances[i], maxLength) * scale;

            int x2 = originX + (int) (length * Math.sin(angleRad));
            int y2 = originY - (int) (length * Math.cos(angleRad));

            g2d.setColor(Color.GREEN);
            g2d.drawLine(originX, originY, x2, y2);

            // Opzionale: etichetta il sensore
            g2d.setColor(Color.RED);
            g2d.drawString(String.valueOf(i), x2, y2);
        }
    }

    public static void showRadar(RadarVisualizer radar) {
        JFrame frame = new JFrame("Track Edge Sensor Radar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.add(radar);
        frame.setVisible(true);
    }
}
