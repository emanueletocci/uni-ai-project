package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.model.FeatureType;
import it.unisa.diem.ai.torcs.sensors.SensorModel;


public class AutonomousDriver extends BaseDriver {

    private final NearestNeighbor nearestNeighbor;
    private Action action;

    public AutonomousDriver() {
        action = new Action();
        nearestNeighbor = new NearestNeighbor("data/dataset.csv");
    }

    @Override
    public Action control(SensorModel sensors) {
      if (sensors.getSpeed() > 1 || action.gear != -1d)
            action.gear = getGear(sensors);

        if (sensors.getSpeed() < 1 || action.gear == -1)
            action.accelerate = 0.3d;

        action.brake = filterABS(sensors, (float) action.brake);
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

        int dataClass = nearestNeighbor.classify(dataSample, 3);
switch (dataClass) {
				case 0 : {
					accelera(sensors, action);
					break;
				}
				case 1 : {
					frena(action);
					break;
				}
				case 2 : {
					sterzaSX(action);
					break;
				}
				case 3 : {
					sterzaDX(action);
					break;
				}
				case 4 : {
					retro(action);
					break;
				}
				case 5 : {
					setDefault(action);
					break;
				}
        	}

        return action;
    
    }

    public void accelera(SensorModel sensor, Action action){
        action.accelerate = 1.0;
        action.steering = 0.0;
        action.brake = 0.0;
    }

    public void frena(Action action){
        action.brake = 0.8;
        action.accelerate = 0.0;
        action.steering = 0.0;
    }

    public void sterzaSX(Action action){
        action.steering = +0.5;
        action.accelerate = 0.25;
        action.brake = 0.0;
    }

    public void sterzaDX(Action action){
        action.steering = -0.5;
        action.accelerate = 0.25;
        action.brake = 0.0;
    }

    public void retro(Action action){
        action.gear = -1;
        action.accelerate = 0.6;	
        action.steering = 0.0;
        action.brake = 0.0;		
    }

    public void setDefault(Action action){
        action.accelerate = 0.3;
        action.steering = 0.0;
        action.brake = 0.0;
    }
}
