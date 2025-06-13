package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Action;
import it.unisa.diem.ai.torcs.model.ClassLabel;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.DataLogger;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;

public class SimpleDriver extends BaseDriver {

    private final DataLogger logger;

    public SimpleDriver() {
        super();
        logger = new DataLogger("data/dataset.csv");
    }

    public Action control(SensorModel sensors) {
        if (Math.abs(sensors.getAngleToTrackAxis()) > stuckAngle) {
            stuck++;
        } else {
            stuck = 0;
        }

        Action action = new Action();

        if (stuck > stuckTime) {
            float steer = (float) (-sensors.getAngleToTrackAxis() / steerLock);
            int gear = -1;
            if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
                gear = 1;
                steer = -steer;
            }
            clutch = clutching(sensors, clutch);
            action.gear = gear;
            action.steering = steer;
            action.accelerate = 1.0;
            action.brake = 0;
            action.clutch = clutch;
        } else {
            float accel_and_brake = getAccel(sensors);
            int gear = getGear(sensors);
            float steer = getSteer(sensors);
            if (steer < -1) steer = -1;
            if (steer > 1) steer = 1;
            float accel, brake;
            if (accel_and_brake > 0) {
                accel = accel_and_brake;
                brake = 0;
            } else {
                accel = 0;
                brake = filterABS(sensors, -accel_and_brake);
            }
            clutch = clutching(sensors, clutch);
            action.gear = gear;
            action.steering = steer;
            action.accelerate = accel;
            action.brake = brake;
            action.clutch = clutch;
        }

        // --- LOGGING PER BEHAVIORAL CLONING ---
        double[] track = sensors.getTrackEdgeSensors();
        double trackPos = sensors.getTrackPosition();
        double angle = sensors.getAngleToTrackAxis();
        double speedX = sensors.getSpeed();
        double speedY = sensors.getLateralSpeed();

        int classLabel = ClassLabel.calculateLabel(action).getCode();
        double[] features = FeatureNormalizer.extractAndNormalizeFeatures(track, trackPos, angle, speedX, speedY);
        logger.logFeaturesNormalizzate(features, classLabel);

        return action;
    }
}
