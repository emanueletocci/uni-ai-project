package it.unisa.diem.ai.torcs.utilities;

import javax.swing.*;

import it.unisa.diem.ai.torcs.controllers.SimpleDriverManual;
import it.unisa.diem.ai.torcs.actions.Action;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * GUI che legge i tasti WASD e aggiorna la classe KeyInput.
 */
public class ContinuousCharReaderUI extends JFrame {

    private final SimpleDriverManual driver; //Oggetto identificante la vettura
    private final Action action; // Azione associata al driver

    private KeyInput notifica;
    private final Set<Integer> pressedKeys = new HashSet<>();

    private JTextField inputField;

    public ContinuousCharReaderUI(SimpleDriverManual driver) {
        this.driver = driver;
        this.action = driver.action;
        this.notifica = new KeyInput();


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
                pressedKeys.add(e.getKeyCode());
                driver.pulsante = e.getKeyCode(); ////DA METTERE PULSANTE COME ATTRIBUTO 

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        notifica.up = true;
                        action.accelerate = 1.0;  
                        break;
                    case KeyEvent.VK_SPACE:
                        notifica.brake = true;
                        action.brake = 1.0;
                        break;
                    case KeyEvent.VK_A:
                        notifica.left = true;
                        action.steering = +0.5;
                        break;
                    case KeyEvent.VK_D:
                        notifica.right = true;
                        action.steering = -0.5;
                        break;
                    case KeyEvent.VK_S: //shift for reverse
                        notifica.down = true;
                        action.gear = -1;
                        break;
                }
            }

            // Gestione del rilascio dei tasti
            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        notifica.up = false;
                        action.accelerate = 0.0;
                        break;
                    case KeyEvent.VK_SPACE:
                        notifica.brake = false;
                        action.brake = 0.0;
                        break;
                    case KeyEvent.VK_A:
                        notifica.left = false;
                        action.steering = 0.0;
                        break;
                    case KeyEvent.VK_D:
                        notifica.right = false;
                        action.steering = 0.0;
                        break;
                    case KeyEvent.VK_S:
                        notifica.down = false;
                        action.gear = 1;
                        action.accelerate = 0.0;
                        break;
                }

                if (pressedKeys.isEmpty()) {
                    driver.pulsante = -1;
                } else {
                    updateLastPressedKey();
                }
            }

            /*
             * Metodo aggiunto:
             * Questo metodo serve ad aggiornare nel SimpleDriver, 
             * così da comunicare tramite la variabile ch, l'ultimo tasto premuto.
             */
            private void updateLastPressedKey() {
                if (notifica.up) {
                    driver.pulsante = KeyEvent.VK_W;
                } else if (notifica.brake) {
                    driver.pulsante = KeyEvent.VK_SPACE;
                } else if (notifica.left) {
                    driver.pulsante = KeyEvent.VK_A;
                } else if (notifica.right) {
                    driver.pulsante = KeyEvent.VK_D;
                } else if (notifica.down) {
                    driver.pulsante = KeyEvent.VK_S;
                }
            }
        });
        // Mostra la GUI e imposta focus iniziale
        setVisible(true);

    }

}