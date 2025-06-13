package it.unisa.diem.ai.torcs.agent;
import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.model.SensorModel;

public abstract class BaseDriver extends Controller{
    final int[] gearUp = { 5000, 6000, 6000, 6500, 7000, 0 };
    final int[] gearDown = { 0, 2500, 3000, 3000, 3500, 3500 };

    final int stuckTime = 25;
    final float stuckAngle = (float) 0.523598775; // PI/6

    final float maxSpeedDist = 70;
    final float maxSpeed = 150;
    final float sin5 = (float) 0.08716;
    final float cos5 = (float) 0.99619;

    final float steerLock = (float) 0.785398;
    final float steerSensitivityOffset = (float) 80.0;
    final float wheelSensitivityCoeff = 1;

    final float[] wheelRadius = { (float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276 };
    final float absSlip = (float) 2.0;
    final float absRange = (float) 3.0;
    final float absMinSpeed = (float) 3.0;

    final float clutchMax = (float) 0.5;
    final float clutchDelta = (float) 0.05;
    final float clutchRange = (float) 0.82;
    final float clutchDeltaTime = (float) 0.02;
    final float clutchDeltaRaced = 10;
    final float clutchDec = (float) 0.01;
    final float clutchMaxModifier = (float) 1.3;
    final float clutchMaxTime = (float) 1.5;

    int stuck = 0;
    float clutch = 0;

    public BaseDriver() {
        super();
    }

    int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();
        if (gear < 1) return 1;
        if (gear < 6 && rpm >= gearUp[gear - 1]) return gear + 1;
        if (gear > 1 && rpm <= gearDown[gear - 1]) return gear - 1;
        return gear;
    }

    float getSteer(SensorModel sensors) {
        float targetAngle = (float) (sensors.getAngleToTrackAxis() - sensors.getTrackPosition() * 0.5);
        if (sensors.getSpeed() > steerSensitivityOffset)
            return (float) (targetAngle / (steerLock * (sensors.getSpeed() - steerSensitivityOffset) * wheelSensitivityCoeff));
        else
            return (targetAngle) / steerLock;
    }

    float getAccel(SensorModel sensors) {
        if (sensors.getTrackPosition() > -1 && sensors.getTrackPosition() < 1) {
            float rxSensor = (float) sensors.getTrackEdgeSensors()[10];
            float sensorsensor = (float) sensors.getTrackEdgeSensors()[9];
            float sxSensor = (float) sensors.getTrackEdgeSensors()[8];

            float targetSpeed;
            if (sensorsensor > maxSpeedDist || (sensorsensor >= rxSensor && sensorsensor >= sxSensor))
                targetSpeed = maxSpeed;
            else {
                if (rxSensor > sxSensor) {
                    float h = sensorsensor * sin5;
                    float b = rxSensor - sensorsensor * cos5;
                    float sinAngle = b * b / (h * h + b * b);
                    targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
                } else {
                    float h = sensorsensor * sin5;
                    float b = sxSensor - sensorsensor * cos5;
                    float sinAngle = b * b / (h * h + b * b);
                    targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
                }
            }
            return (float) (2 / (1 + Math.exp(sensors.getSpeed() - targetSpeed)) - 1);
        } else
            return (float) 0.3;
    }

    float clutching(SensorModel sensors, float clutch) {
        float maxClutch = clutchMax;
        if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE
                && sensors.getDistanceRaced() < clutchDeltaRaced)
            clutch = maxClutch;

        if (clutch > 0) {
            double delta = clutchDelta;
            if (sensors.getGear() < 2) {
                delta /= 2;
                maxClutch *= clutchMaxModifier;
                if (sensors.getCurrentLapTime() < clutchMaxTime)
                    clutch = maxClutch;
            }
            clutch = Math.min(maxClutch, clutch);
            if (clutch != maxClutch) {
                clutch -= (float) delta;
                clutch = Math.max((float) 0.0, clutch);
            } else
                clutch -= clutchDec;
        }
        return clutch;
    }

    float filterABS(SensorModel sensors, float brake) {
        float speed = (float) (sensors.getSpeed() / 3.6);
        if (speed < absMinSpeed)
            return brake;
        float slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += (float) (sensors.getWheelSpinVelocity()[i] * wheelRadius[i]);
        }
        slip = speed - slip / 4.0f;
        if (slip > absSlip) {
            brake = brake - (slip - absSlip) / absRange;
        }
        if (brake < 0)
            return 0;
        else
            return brake;
    }

    void accelera(Action action, SensorModel sensors) {
        action.accelerate = 1.0;
        action.brake = 0.0;
        action.steering = 0f;
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
    }

    void frena(Action action, SensorModel sensors) {
        action.brake = filterABS(sensors, 1f);
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
        action.accelerate = 0.0f;
    }

    void retromarcia(Action action, SensorModel sensors) {
        action.gear = -1;
        action.accelerate = 1.0f;
        action.brake = 0.0;
        action.clutch = clutchMax;
    }

    void giraSinistra(Action action, SensorModel sensors) {
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
        action.steering = 0.3f;
    }

    void giraDestra(Action action, SensorModel sensors) {
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
        action.steering = -0.3f;
    }

    @Override
    public void reset() {
        System.out.println("Restarting the race!");
    }

    @Override
    public void shutdown() {
        System.out.println("Bye bye!");
    }
}