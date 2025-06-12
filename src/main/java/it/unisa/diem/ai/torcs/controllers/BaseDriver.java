package it.unisa.diem.ai.torcs.controllers;
import it.unisa.diem.ai.torcs.Action;
import it.unisa.diem.ai.torcs.model.FeatureType;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import java.util.List;

public abstract class BaseDriver extends Controller{
    final int[] gearUp = { 5000, 6000, 6000, 6500, 7000, 0 };
    final int[] gearDown = { 0, 2500, 3000, 3000, 3500, 3500 };

    final int stuckTime = 100;
    final float stuckAngle = (float) 0.523598775; // PI/6

    final float maxSpeedDist = 70;
    final float maxSpeed = 300;
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

    static final float STERZATA = 0.2f;
    static final float FRENATA = 0.7f;
    static final float ACCELERAZIONE = 1f;

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

    @Override
    public void reset() {
        System.out.println("Restarting the race!");
    }

    @Override
    public void shutdown() {
        System.out.println("Bye bye!");
    }

    // Azioni di guida semplificate
    // Dritto (accelera)
    void accelera(Action action, SensorModel sensors) {
        System.out.println("Accelera!");
        action.accelerate = ACCELERAZIONE;
        action.brake = 0;
    }

    // Gira a sinistra (unico metodo)
    void giraSinistra(Action action) {
        System.out.println("Gira a sinistra!");
        action.steering = STERZATA;
    }

    // Gira a destra (unico metodo)
    void giraDestra(Action action) {
        System.out.println("Gira a destra!");
        action.steering = -STERZATA;
    }

    // Frena (dritto)
    void frena(Action action, SensorModel sensors) {
        System.out.println("Frena!");
        action.brake = filterABS(sensors, FRENATA); // Freno con ABS
        action.accelerate = 0f;
    }

    // Retromarcia (con correzione angolo)
    void retromarcia(Action action, SensorModel sensors) {
        System.out.println("Retromarcia!");
        action.gear = -1;
        action.accelerate = ACCELERAZIONE/2;
        action.brake = 0f;

        // Correggi la direzione in base all'angolo rispetto all'asse della pista
        action.steering = (float) (-sensors.getAngleToTrackAxis() / (Math.PI / 2));
    }

    // Nessuna azione / decelerazione
    void mantieniVelocita(Action action) {
        System.out.println("Mantieni velocità!");
        action.accelerate = 0f;
        action.brake = 0.0;
        action.steering = 0.0;
    }

    /**
     * Restituisce true se l'auto è fuori pista, ovvero se il valore assoluto della posizione
     * rispetto al centro della pista è maggiore di 1.0.
     *
     * @param trackPos posizione dell'auto rispetto al centro pista (in [-inf, +inf])
     * @return true se l'auto è fuori pista
     */
    boolean isOffTrack(double trackPos) {
        return Math.abs(trackPos) > 1.1;
    }

    /**
     * Rileva se i sensori selezionati presentano valori anomali e notifica immediatamente quale sensore non funziona.
     *
     * @param trackEdgeSensors Array completo dei sensori di bordo pista.
     * @return true se almeno un sensore selezionato è anomalo, false altrimenti.
     */
    boolean detectSensorAnomalies(double[] trackEdgeSensors) {
        List<Integer> indices = FeatureType.getTrackSensorIndices();
        boolean anyAnomaly = false;

        for (int idx : indices) {
            double value = trackEdgeSensors[idx];
            if (value <= 0 || value > 200) {
                System.out.println("⚠️ ALLARME: Sensore " + idx + " anomalo! Valore: " + value);
                anyAnomaly = true;
            }
        }
        return anyAnomaly;
    }

}
