package it.unisa.diem.ai.torcs.controllers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.model.FeatureType;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utilities.DataLogger;

import java.awt.event.KeyEvent;

public class SimpleDriverManual extends BaseDriver{

    public Action action;//Azione controllante la vettura
	public int pulsante;//variabile per salvare il codice relativo al tasto premuto 
    private final DataLogger fileWriter;

public SimpleDriverManual() {
        super();
this.fileWriter = new DataLogger("data/dataset.csv");
       
			SwingUtilities.invokeLater(() -> new ContinuousCharReaderUI(this));
		
    
    }


    @Override
	public Action control(SensorModel sensors) {
		if(action.gear != -1.0 || sensors.getSpeed() > 1 ) {
			action.gear = getGear(sensors);
		}
		else if(action.gear == -1 || sensors.getSpeed() < 1 ) {
			action.accelerate = 1.0;
		}
		action.brake = filterABS(sensors, (float) action.brake);
		//con o senza clutching non cambia niente
		action.clutch = clutching(sensors, (float) action.clutch);			

       
        Sample dataSample = new Sample();
        dataSample.set(FeatureType.SPEED, sensors.getSpeed());
        dataSample.set(FeatureType.TRACK_POSITION, sensors.getTrackPosition());
        dataSample.set(FeatureType.ANGLE_TO_TRACK_AXIS, sensors.getAngleToTrackAxis());
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_4, sensors.getTrackEdgeSensors()[4]);
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_6, sensors.getTrackEdgeSensors()[6]);
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_8, sensors.getTrackEdgeSensors()[8]);
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_9, sensors.getTrackEdgeSensors()[9]);
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_10, sensors.getTrackEdgeSensors()[10]);
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_12, sensors.getTrackEdgeSensors()[12]);
        dataSample.set(FeatureType.TRACK_EDGE_SENSORS_14, sensors.getTrackEdgeSensors()[14]);


        dataSample.normalize(FeatureType.SPEED, sensors.getSpeed() < 0 ? -60d : 0d, sensors.getSpeed() < 0 ? -0.001d : 310d);
        dataSample.normalize(FeatureType.TRACK_POSITION, -1d, 1d);
        dataSample.normalize(FeatureType.ANGLE_TO_TRACK_AXIS, -Math.PI, Math.PI);
        dataSample.normalize(FeatureType.TRACK_EDGE_SENSORS_4, 0d, 200d);
        dataSample.normalize(FeatureType.TRACK_EDGE_SENSORS_6, 0d, 200d);
        dataSample.normalize(FeatureType.TRACK_EDGE_SENSORS_8, 0d, 200d);
        dataSample.normalize(FeatureType.TRACK_EDGE_SENSORS_9, 0d, 200d);
        dataSample.normalize(FeatureType.TRACK_EDGE_SENSORS_10, 0d, 200d);
        dataSample.normalize(FeatureType.TRACK_EDGE_SENSORS_12, 0d, 200d);
        dataSample.normalize(FeatureType.TRACK_EDGE_SENSORS_14, 0d, 200d);

        switch (pulsante) {
            case KeyEvent.VK_W: dataSample.dataClass = 0; break;
            case KeyEvent.VK_SPACE: dataSample.dataClass = 1; break;
            case KeyEvent.VK_A: dataSample.dataClass = 2; break;
            case KeyEvent.VK_D: dataSample.dataClass = 3; break;
            case KeyEvent.VK_S: dataSample.dataClass = 4; break;
            default: dataSample.dataClass = 5;
        }
fileWriter.log(dataSample.toString());



		return action;
	}

    

}
