package it.unisa.diem.ai.torcs.io;

import it.unisa.diem.ai.torcs.model.KeyInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Finestra grafica che legge in tempo reale i tasti premuti (WASD + spazio)
 * e aggiorna lo stato della classe {@link KeyInput}, permettendo il controllo
 * manuale del veicolo in TORCS.
 */
public class ContinuousCharReaderUI extends JFrame {

    private static ContinuousCharReaderUI instance; // Singleton instance

    /** Campo di testo invisibile che intercetta gli eventi da tastiera */
    private JTextField inputField;

    /** Checkbox per abilitare/disabilitare la registrazione del dataset */
    private JCheckBox recordDatasetCheckbox;

    /**
     * Costruttore della GUI. Imposta layout, dimensioni, focus, listener tastiera e checkbox.
     */
    public ContinuousCharReaderUI() {
        instance = this; // Imposta l'istanza singleton

        setTitle("TORCS Manual Controller");
        setSize(300, 120);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Campo testo invisibile per intercettare input tastiera
        inputField = new JTextField(20);
        add(inputField);

        // Checkbox per registrazione dataset
        recordDatasetCheckbox = new JCheckBox("Registra dataset");
        add(recordDatasetCheckbox);

        // Garantisce che il campo di input riceva il focus
        inputField.setFocusable(true);
        inputField.requestFocusInWindow();

        // Listener per gestire la pressione dei tasti
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (Character.toLowerCase(e.getKeyChar())) {
                    case 'w': KeyInput.up = true; break;
                    case 'a': KeyInput.left = true; break;
                    case 's': KeyInput.down = true; break;
                    case 'd': KeyInput.right = true; break;
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    KeyInput.brake = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (Character.toLowerCase(e.getKeyChar())) {
                    case 'w': KeyInput.up = false; break;
                    case 'a': KeyInput.left = false; break;
                    case 's': KeyInput.down = false; break;
                    case 'd': KeyInput.right = false; break;
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    KeyInput.brake = false;
                }
            }
        });

        setVisible(true);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    /**
     * Metodo statico per ottenere l'istanza singleton di questa GUI.
     */
    public static ContinuousCharReaderUI getInstance() {
        return instance;
    }

    /**
     * Metodo che ritorna true se la checkbox "Registra dataset" è selezionata.
     */
    public boolean isDatasetRecordingEnabled() {
        return recordDatasetCheckbox != null && recordDatasetCheckbox.isSelected();
    }

    /**
     * Metodo main per avviare la GUI in modo indipendente.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }
}
