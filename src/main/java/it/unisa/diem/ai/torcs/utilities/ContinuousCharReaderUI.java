package it.unisa.diem.ai.torcs.utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * GUI che legge i tasti WASD e aggiorna la classe KeyInput.
 * Include anche una checkbox per attivare la registrazione del dataset recovery.
 */
public class ContinuousCharReaderUI extends JFrame {

    private JTextField inputField;
    private JCheckBox logCheckbox;

    public ContinuousCharReaderUI() {
        setTitle("TORCS Manual Controller");
        setSize(320, 140);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Campo input
        inputField = new JTextField(20);
        add(inputField);

        // Checkbox per logging
        logCheckbox = new JCheckBox("📝 Registra recovery");
        logCheckbox.setFocusable(false); // non interferisce con il focus
        logCheckbox.addActionListener(e -> {
            KeyInput.logRecovery = logCheckbox.isSelected();
            System.out.println("🚨 Log recovery " + (KeyInput.logRecovery ? "ATTIVO" : "disattivato"));
        });
        add(logCheckbox);

        // Garantisce che il campo possa ricevere focus
        inputField.setFocusable(true);
        inputField.requestFocusInWindow();

        // Listener tasti premuti/rilasciati
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (Character.toLowerCase(e.getKeyChar())) {
                    case 'w' -> KeyInput.up = true;
                    case 'a' -> KeyInput.left = true;
                    case 's' -> KeyInput.down = true;
                    case ' ' -> KeyInput.brake = true;
                    case 'd' -> KeyInput.right = true;
                }
                System.out.println("Pressed: " + e.getKeyChar());
                System.out.println("Key state → W: " + KeyInput.up + " | A: " + KeyInput.left + " | S: " + KeyInput.down + " | D: " + KeyInput.right);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (Character.toLowerCase(e.getKeyChar())) {
                    case 'w' -> KeyInput.up = false;
                    case 'a' -> KeyInput.left = false;
                    case 's' -> KeyInput.down = false;
                    case 'd' -> KeyInput.right = false;
                    case ' ' -> KeyInput.brake = false;
                }
                System.out.println("Released: " + e.getKeyChar());
                System.out.println("Key state → W: " + KeyInput.up + " | A: " + KeyInput.left + " | S: " + KeyInput.down + " | D: " + KeyInput.right);
            }
        });

        // Mostra GUI
        setVisible(true);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }
}