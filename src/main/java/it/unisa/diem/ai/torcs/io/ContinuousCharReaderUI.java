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

    /** Campo di testo invisibile che intercetta gli eventi da tastiera */
    private JTextField inputField;

    /**
     * Costruttore della GUI. Imposta layout, dimensioni, focus e listener tastiera.
     */
    public ContinuousCharReaderUI() {
        setTitle("TORCS Manual Controller");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        inputField = new JTextField(20);
        add(inputField);

        // Garantisce che il campo di input riceva il focus
        inputField.setFocusable(true);
        inputField.requestFocusInWindow();

        // Listener per gestire la pressione dei tasti
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Converte il tasto in minuscolo e aggiorna i flag
                switch (Character.toLowerCase(e.getKeyChar())) {
                    case 'w': KeyInput.up = true; break;
                    case 'a': KeyInput.left = true; break;
                    case 's': KeyInput.down = true; break;
                    case 'd': KeyInput.right = true; break;
                    case KeyEvent.VK_SPACE: KeyInput.brake = true; break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Rilascia i flag quando il tasto non è più premuto
                switch (Character.toLowerCase(e.getKeyChar())) {
                    case 'w': KeyInput.up = false; break;
                    case 'a': KeyInput.left = false; break;
                    case 's': KeyInput.down = false; break;
                    case 'd': KeyInput.right = false; break;
                    case KeyEvent.VK_SPACE: KeyInput.brake = false; break;
                }
            }
        });

        // Mostra la finestra e assicura che il focus sia sul campo
        setVisible(true);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    /**
     * Metodo main per avviare la GUI in modo indipendente.
     *
     * @param args argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }
}
