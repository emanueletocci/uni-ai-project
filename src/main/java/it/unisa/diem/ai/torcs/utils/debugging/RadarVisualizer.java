package it.unisa.diem.ai.torcs.utils.debugging;

import it.unisa.diem.ai.torcs.model.SensorFeature;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Classe per visualizzare graficamente i sensori di bordo pista (track edge sensors)
 * in una rappresentazione tipo radar.
 */
public class RadarVisualizer extends JPanel {

    /** Array di 19 distanze, una per ciascun sensore di bordo pista. */
    private double[] distances = new double[19];

    /**
     * Aggiorna i valori dei sensori e ridisegna il radar.
     *
     * @param newDistances array di 19 valori rappresentanti le distanze rilevate dai sensori.
     */
    public void updateSensors(double[] newDistances) {
        this.distances = newDistances;
        repaint();
    }

    /**
     * Metodo chiamato automaticamente per disegnare il componente radar.
     *
     * @param g oggetto grafico fornito dal sistema Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        // Punto di origine: centro in basso
        int originX = width / 2;
        int originY = height - 20;

        // Scala di conversione metri → pixel
        double maxLength = 100.0;
        double scale = (height - 40) / maxLength;

        // Ottieni solo gli indici dei sensori selezionati
        List<Integer> indices = SensorFeature.getTrackSensorIndices();

        for (int i : indices) {
            double angleDeg = -90 + i * 10; // angoli: -90° a +90°
            double angleRad = Math.toRadians(angleDeg);
            double length = Math.min(distances[i], maxLength) * scale;

            int x2 = originX + (int) (length * Math.sin(angleRad));
            int y2 = originY - (int) (length * Math.cos(angleRad));

            // Linea del sensore
            g2d.setColor(Color.GREEN);
            g2d.drawLine(originX, originY, x2, y2);

            // Etichetta del sensore
            g2d.setColor(Color.RED);
            g2d.drawString(String.valueOf(i), x2, y2);
        }
    }

    /**
     * Mostra una finestra Swing con il pannello radar.
     *
     * @param radar istanza di RadarVisualizer da visualizzare
     */
    public static void showRadar(RadarVisualizer radar) {
        JFrame frame = new JFrame("Track Edge Sensor Radar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.add(radar);
        frame.setVisible(true);
    }
}
