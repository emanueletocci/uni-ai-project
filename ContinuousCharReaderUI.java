import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ContinuousCharReaderUI extends JFrame {
    private JTextField inputField;

    public ContinuousCharReaderUI() {
        // Set up the frame
        setTitle("Continuous Character Reader");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Initialize the text field for input
        inputField = new JTextField(20);
        add(inputField);

        // Add key listener to the text field
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char ch = e.getKeyChar();
                System.out.println("You pressed: " + ch);

                // Clear the text field
                inputField.setText("");

                // Exit if 'q' is pressed
                if (ch == 'q') {
                    System.exit(0);
                }
            }
        });

        // Make the frame visible
        setVisible(true);
    }

    public static void main(String[] args) {
        // Run the UI in the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new ContinuousCharReaderUI());
    }
}

