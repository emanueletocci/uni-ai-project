package it.unisa.diem.ai.torcs.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DataLogger {
    private PrintWriter writer;
    private boolean headerWritten = false;

    public DataLogger(String filename) throws IOException {
        File file = new File(filename);
        headerWritten = file.exists() && file.length() > 0;
        writer = new PrintWriter(new FileWriter(file, true));
        if (!headerWritten) {
            writeHeader();
        }
    }

    private void writeHeader() {
        // Scrivi l'intestazione esattamente come nel tuo esempio
        writer.println("speed;trackPosition;trackEdgeSensor4;trackEdgeSensor6;trackEdgeSensor8;trackEdgeSensor9;trackEdgeSensor10;trackEdgeSensor12;trackEdgeSensor14;angleToTrackAxis;classLabel");
        writer.flush();
        headerWritten = true;
    }

    public void log(double speed, double trackPosition,
                    double[] trackEdgeSensors, // lunghezza 19
                    double angleToTrackAxis, int classLabel) {
        // Estrai solo i sensori richiesti
        StringBuilder sb = new StringBuilder();
        sb.append(speed).append(";")
                .append(trackPosition).append(";")
                .append(trackEdgeSensors[4]).append(";")
                .append(trackEdgeSensors[6]).append(";")
                .append(trackEdgeSensors[8]).append(";")
                .append(trackEdgeSensors[9]).append(";")
                .append(trackEdgeSensors[10]).append(";")
                .append(trackEdgeSensors[12]).append(";")
                .append(trackEdgeSensors[14]).append(";")
                .append(angleToTrackAxis).append(";")
                .append(classLabel);
        writer.println(sb.toString());
        writer.flush();
    }

    public void close() {
        writer.close();
    }
}
