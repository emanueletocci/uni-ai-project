package it.unisa.diem.ai.torcs.io;

import it.unisa.diem.ai.torcs.model.KeyInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * GUI che legge i tasti WASD e aggiorna la classe KeyInput.
 */
public class ContinuousCharReaderUI extends JFrame {

    private JTextField inputField;

    public ContinuousCharReaderUI() {
        setTitle("TORCS Manual Controller");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        inputField = new JTextField(20);
        add(inputField);

        // Garantisce che il campo possa ricevere focus
        inputField.setFocusable(true);
        inputField.requestFocusInWindow();

        // Listener per i tasti premuti e rilasciati
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
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
                switch (Character.toLowerCase(e.getKeyChar())) {
                    case 'w': KeyInput.up = false; break;
                    case 'a': KeyInput.left = false; break;
                    case 's': KeyInput.down = false; break;
                    case 'd': KeyInput.right = false; break;
                    case KeyEvent.VK_SPACE: KeyInput.brake = false; break;

                }
            }
        });

        // Mostra la GUI e imposta focus iniziale
        setVisible(true);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }
}