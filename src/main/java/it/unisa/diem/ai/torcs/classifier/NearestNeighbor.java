/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unisa.diem.ai.torcs.classifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unisa.diem.ai.torcs.Sample;

public class NearestNeighbor {
    
    private List<Sample> trainingData;
    private String firstLineOfTheFile;

    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.firstLineOfTheFile = "Speed; DistanzaLineaCentrale; SensoreSX1; SensoreSX2; SensoreCentrale; SensoreDX1; SensoreDX2; Angolo; Classe";
        this.readPointsFromCSV(filename);
    }

    private void readPointsFromCSV(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(firstLineOfTheFile)) {
                    continue; // Skip header
                }
                // Add the sample by calling the constructor that takes the string input
                trainingData.add(new Sample(line));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int classify(Sample targetPoint){
        if (trainingData.isEmpty()) {
            System.out.println("training set vuoto");

           return -1; 
       }

       Sample nearestNeighbor = trainingData.get(0); 
       double minDistance = targetPoint.distance(nearestNeighbor); 

       // Cerca il punto più vicino
       for (Sample point : trainingData) {
           double distance = targetPoint.distance(point);
           if (distance < minDistance) {
               minDistance = distance;
               nearestNeighbor = point;
           }
       }

       return nearestNeighbor.cls;
    }

    public List<Sample> getTrainingData() {
        return trainingData;
    }
}
