package it.unisa.diem.ai.torcs.io;

import it.unisa.diem.ai.torcs.model.KeyInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Interfaccia grafica minimale per il controllo manuale dell’auto in TORCS.
 * Permette di utilizzare la tastiera per guidare e di abilitare/disabilitare
 * la registrazione del dataset tramite checkbox.
 */
public class ContinuousCharReaderUI extends JFrame {

    /** Istanza singleton dell’interfaccia. */
    private static ContinuousCharReaderUI instance;

    /** Checkbox per abilitare/disabilitare la registrazione del dataset. */
    private JCheckBox recordDatasetCheckbox;

    /**
     * Costruttore dell’interfaccia.
     * Inizializza il layout, i listener da tastiera e i controlli grafici.
     */
    public ContinuousCharReaderUI() {
        instance = this;
        this.setTitle("TORCS Manual Controller");
        this.setSize(300, 120);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout());

        // Pannello per la cattura degli input da tastiera
        JPanel inputPanel = new JPanel();
        inputPanel.setPreferredSize(new Dimension(200, 30));
        inputPanel.setFocusable(true);
        inputPanel.requestFocusInWindow();
        inputPanel.setBackground(Color.LIGHT_GRAY);
        this.add(inputPanel);

        // Checkbox per abilitare/disabilitare la registrazione del dataset
        this.recordDatasetCheckbox = new JCheckBox("Registra dataset");
        this.add(this.recordDatasetCheckbox);

        // Ripristina il focus sul pannello di input dopo click sulla checkbox
        recordDatasetCheckbox.addActionListener(e -> inputPanel.requestFocusInWindow());

        // Listener per gestire i tasti premuti e rilasciati
        inputPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A -> KeyInput.left = true;
                    case KeyEvent.VK_D -> KeyInput.right = true;
                    case KeyEvent.VK_W -> KeyInput.up = true;
                    case KeyEvent.VK_S -> KeyInput.down = true;
                    case KeyEvent.VK_B -> KeyInput.brake = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A -> KeyInput.left = false;
                    case KeyEvent.VK_D -> KeyInput.right = false;
                    case KeyEvent.VK_W -> KeyInput.up = false;
                    case KeyEvent.VK_S -> KeyInput.down = false;
                    case KeyEvent.VK_B -> KeyInput.brake = false;
                }
            }
        });

        this.setVisible(true);

        // Forza il focus sul pannello di input all'avvio
        SwingUtilities.invokeLater(inputPanel::requestFocusInWindow);
    }

    /**
     * Restituisce l’istanza corrente dell’interfaccia grafica.
     *
     * @return istanza di {@code ContinuousCharReaderUI}
     */
    public static ContinuousCharReaderUI getInstance() {
        return instance;
    }

    /**
     * Verifica se la registrazione del dataset è attiva (checkbox selezionata).
     *
     * @return {@code true} se la registrazione è abilitata, altrimenti {@code false}
     */
    public boolean isDatasetRecordingEnabled() {
        return recordDatasetCheckbox != null && recordDatasetCheckbox.isSelected();
    }

    /**
     * Avvia l’interfaccia come applicazione standalone.
     *
     * @param args argomenti da linea di comando (non utilizzati)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }
}
