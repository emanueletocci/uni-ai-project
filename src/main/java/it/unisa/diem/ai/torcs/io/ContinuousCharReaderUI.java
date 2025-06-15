package it.unisa.diem.ai.torcs.io;

import it.unisa.diem.ai.torcs.model.KeyInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ContinuousCharReaderUI extends JFrame {
    private static ContinuousCharReaderUI instance;
    private JCheckBox recordDatasetCheckbox;


    public ContinuousCharReaderUI() {
        instance = this;
        this.setTitle("TORCS Manual Controller");
        this.setSize(300, 120);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout());

        // Pannello per input da tastiera (al posto del JTextField)
        JPanel inputPanel = new JPanel();
        inputPanel.setPreferredSize(new Dimension(200, 30));
        inputPanel.setFocusable(true);
        inputPanel.requestFocusInWindow();
        inputPanel.setBackground(Color.LIGHT_GRAY);
        this.add(inputPanel);

        this.recordDatasetCheckbox = new JCheckBox("Registra dataset");
        this.add(this.recordDatasetCheckbox);
        // Quando si clicca la checkbox, riporta il focus sul pannello di input
        recordDatasetCheckbox.addActionListener(e -> inputPanel.requestFocusInWindow());

        // KeyListener con solo getKeyCode()
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

        // Forza il focus sul pannello
        SwingUtilities.invokeLater(inputPanel::requestFocusInWindow);
    }

    public static ContinuousCharReaderUI getInstance() {
        return instance;
    }

    public boolean isDatasetRecordingEnabled() {
        return recordDatasetCheckbox != null && recordDatasetCheckbox.isSelected();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
    }
}