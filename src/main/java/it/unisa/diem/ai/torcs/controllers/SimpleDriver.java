package it.unisa.diem.ai.torcs.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.SwingUtilities;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.ContinuousCharReaderUI;



public class SimpleDriver extends BaseDriver {
    private String fileName= "data/dataset.csv";

    private Action action;
    private char pulsante = ' ';
    private boolean train = false;
    private String firstLineOfTheFile = "Speed; DistanzaLineaCentrale; SensoreSX1; SensoreSX2; SensoreCentrale; SensoreDX1; SensoreDX2; Angolo; Classe\n";
  
     private int classe = -1;
    private double[] features = new double[8];

        private File file;
    private NearestNeighbor nn;
    private boolean guidaAutonoma;

    private double angolo;

    public SimpleDriver(boolean guidaAutonoma) {
        super();
         action = new Action();
        this.guidaAutonoma = guidaAutonoma;

        if (!guidaAutonoma) {
            this.file = new File(fileName);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.append(firstLineOfTheFile); //metti header con get
            } catch (IOException ex) {
            }
            SwingUtilities.invokeLater(() -> new ContinuousCharReaderUI(this));
        } else {
// se non sono in guida automatica allora significa guida automatica
            this.nn = new NearestNeighbor(fileName);
        }
       }
       /**
     *
     * @return
     */
    public char getKey() {
        return pulsante;
    }

    /**
     *
     * @return
     */
    public boolean isTrain() {
        return train;
    }

    /**
     *
     * @param lettura
     */
    public void setTrain(boolean train) {
        this.train = train;
    }
    
    /**
     *
     * @param ch
     */
    public void setKey(char pulsante) {
        this.pulsante = pulsante;
    }

    public double normalizzatoreMinMax(double data, double min, double max) {
        double x = (data - min) / (max - min);
        return x;
    }


    public Action control(SensorModel sensors) {
    

        features[0] = normalizzatoreMinMax(sensors.getSpeed(), 0.0, 250);
        features[1] = normalizzatoreMinMax(sensors.getTrackPosition(), -2.0, 2.0);
        features[2] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[3], -1.0, 200);
        features[3] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[6], -1.0, 200);
        features[4] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[9], -1.0, 200);
        features[5] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[12], -1.0, 200);
        features[6] = normalizzatoreMinMax(sensors.getTrackEdgeSensors()[15], -1.0, 200);
        features[7] = normalizzatoreMinMax(sensors.getAngleToTrackAxis(), -Math.PI, Math.PI);

        angolo = sensors.getAngleToTrackAxis();  

        if (!guidaAutonoma) {
            try {
                writeCSV();
            } catch (Exception ex) {
               System.out.println("Errore accesso CSV"); 
            }
        } else {
            Sample point = new Sample(features);
        int predictedClass = nn.classify(point);
        classe = predictedClass;
        }
        autoControl();
        if (action.gear >= 0){
            action.gear = getGear(sensors);
        }
    
        action.brake = filterABS(sensors, action.brake);
        action.clutch = clutching(sensors, action.clutch);
     

        return action;
    }
        private double filterABS(SensorModel sensors, double brake) {
        // convert speed to m/s
        double speed = (double) (sensors.getSpeed() / 3.6);
        // when spedd lower than min speed for abs do nothing
        if (speed < absMinSpeed) {
            return brake;
        }

        // compute the speed of wheels in m/s
        double slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
        }
        // slip is the difference between actual speed of car and average speed of
        // wheels
        slip = speed - slip / 4.0f;
        // when slip too high applu ABS
        if (slip > absSlip) {
            brake = brake - (slip - absSlip) / absRange;
        }

        // check brake is not negative, otherwise set it to zero
        if (brake < 0) {
            return 0;
        } else {
            return brake;
        }
    }

    /**
     *
     * @param sensors
     * @param clutch
     * @return
     */
    public double clutching(SensorModel sensors, double clutch) {

        double maxClutch = clutchMax;

        // Check if the current situation is the race start
        if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE
                && sensors.getDistanceRaced() < clutchDeltaRaced) {
            clutch = maxClutch;
        }

        // Adjust the current value of the clutch
        if (clutch > 0) {
            double delta = clutchDelta;
            if (sensors.getGear() < 2) {
                // Apply a stronger clutch output when the gear is one and the race is just
                // started
                delta /= 2;
                maxClutch *= clutchMaxModifier;
                if (sensors.getCurrentLapTime() < clutchMaxTime) {
                    clutch = maxClutch;
                }
            }

            // check clutch is not bigger than maximum values
            clutch = Math.min(maxClutch, clutch);

            // if clutch is not at max value decrease it quite quickly
            if (clutch != maxClutch) {
                clutch -= delta;
                clutch = Math.max((double) 0.0, clutch);
            } // if clutch is at max value decrease it very slowly
            else {
                clutch -= clutchDec;
            }
        }
        return clutch;
    }


        public void autoControl() {
        
        switch(classe) {
            case 0:
                accelera();
                break;
            case 1:
                giraSXMolto();
                break;
            case 2:
                giraSX();
                break;
            case 3:
                giraSXPoco();
                break;
            case 4:
                giraDXMolto();
                break;
            case 5:
                giraDX();
                break;
            case 6:
                giraDXPoco();
                break;
            case 7:
                frena();
                break;
            case 8:
                retromarcia();
                break;
            default:
                decelera();
                break;
            
        }
        
    }
       private void accelera() {
        if (action.gear == -1) {
            action.gear = 1;
        }
        action.steering = 0;
        action.brake = 0;
        action.accelerate = 1;
    }
    
    private void giraSXMolto() {
        action.accelerate = 0;
        action.brake = 1;
        action.steering = 0.5;
    }
    
    private void giraSX() {
        action.accelerate = 0.5;
        action.brake = 0;
        action.steering = 0.25;
    }
    
    private void giraSXPoco() {
        action.accelerate = 1;
        action.brake = 0;
        action.steering = 0.1; 
    }
    
    private void giraDXMolto() {
        action.accelerate = 0;
        action.brake = 1;
        action.steering = -0.5;
    }
    
    private void giraDX() {
        action.accelerate = 0.5;
        action.brake = 0;
        action.steering = -0.25;
    }
    
    private void giraDXPoco() {
        action.accelerate = 1;
        action.brake = 0;
        action.steering = -0.1;
    }
    
    private void frena() {
        action.steering = 0;
        action.accelerate = 0;
        action.brake = 1;
    }
    
    private void retromarcia() {
        action.gear = -1;
        action.brake = 0;
        action.accelerate = 0.15;
        action.steering = (float) (-angolo / steerLock);
    }
    
    private void decelera() {
        if (action.gear == -1) {
            action.gear = 1;
        }
        action.accelerate = 0;
        action.brake = 0;
        action.steering = 0;
    }
    

    public void writeCSV() throws Exception {
        int classe;
        if (pulsante == 'w') {
            classe = 0;
        } else if (pulsante == 'a' && features[4] < 0.2 && features[0] > 0.5) {
            classe = 1;
        } else if (pulsante == 'a' && features[4] < 0.5) {
            classe = 2;
        } else if (pulsante == 'a') {
            classe = 3;
        } else if (pulsante == 'd' && features[4] < 0.2  && features[0] > 0.5) {
            classe = 4;
        } else if (pulsante == 'd' && features[4] < 0.5) {
            classe = 5;
        } else if (pulsante == 'd') {
            classe = 6;
        } else if (pulsante == 's') {
            classe = 7;
        } else if (pulsante == 'r') {
            classe = 8;
        } else {
            classe = 9;
        }
        
        if (train) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                bw.append(features[0] + "; ");
                bw.append(features[1] + "; ");
                bw.append(features[2] + "; ");
                bw.append(features[3] + "; ");
                bw.append(features[4] + "; ");
                bw.append(features[5] + "; ");
                bw.append(features[6] + "; ");
                bw.append(features[7] + "; ");
                bw.append(String.valueOf(classe) + '\n');
            }
        }
    }
    
    /*
    @Override
    public float[] initAngles() {
        float[] angles = new float[19];
        for (int i = 0; i < 5; i++) {
            angles[i] = -90 + i * 15;
            angles[18 - i] = 90 - i * 15;
        }
        for (int i = 5; i < 9; i++) {
            angles[i] = -20 + (i - 5) * 5;
            angles[18 - i] = 20 - (i - 5) * 5;
        }
        angles[9] = 0;
        return angles;
    }

     */
}
