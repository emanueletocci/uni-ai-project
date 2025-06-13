package it.unisa.diem.ai.torcs.utilities;

import javax.swing.*;

import it.unisa.diem.ai.torcs.controllers.SimpleDriver;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * GUI che legge i tasti WASD e aggiorna la classe KeyInput.
 */
public class ContinuousCharReaderUI extends JFrame {

    private JTextField inputField;
    private final SimpleDriver driverManual;

    public ContinuousCharReaderUI(SimpleDriver driver) {
        this.driverManual =  driver;

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
            public void keyTyped(KeyEvent e) {
                char pulsante = e.getKeyChar();
                // Clear the text field
                inputField.setText("");
                driverManual.setKey(pulsante);
                if(pulsante == 'l'){
                    boolean lettura = driverManual.isTrain();
                    driverManual.setTrain(!lettura);
                }
                
            }
        });        
        // Mostra la GUI e imposta focus iniziale
        setVisible(true);
  }

}