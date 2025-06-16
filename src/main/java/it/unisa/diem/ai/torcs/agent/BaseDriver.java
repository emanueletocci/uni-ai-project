package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.model.SensorModel;

/**
 * Classe base per i driver di guida autonoma o manuale in TORCS.
 * Fornisce metodi utility condivisi per gestione cambio marcia, sterzata,
 * accelerazione, freno, ABS, frizione e strategie di manovra.
 */
public abstract class BaseDriver extends Controller {

    /** Soglie RPM per il passaggio alla marcia superiore. */
    final int[] gearUp = { 5000, 6000, 6000, 6500, 7000, 0 };

    /** Soglie RPM per la scalata di marcia. */
    final int[] gearDown = { 0, 2500, 3000, 3000, 3500, 3500 };

    /** Numero di cicli per rilevare una situazione di blocco. */
    final int stuckTime = 100;

    /** Angolo soglia per considerare l'auto bloccata. */
    final float stuckAngle = 0.523598775f; // PI/6

    /** Distanza oltre la quale viene raggiunta la velocità massima. */
    final float maxSpeedDist = 70;

    /** Velocità massima del veicolo. */
    final float maxSpeed = 150;

    final float sin5 = 0.08716f;
    final float cos5 = 0.99619f;

    /** Massimo angolo di sterzata. */
    final float steerLock = 0.785398f;

    /** Offset di sensibilità per la sterzata. */
    final float steerSensitivityOffset = 80.0f;

    /** Coefficiente di sensibilità del volante. */
    final float wheelSensitivityCoeff = 1f;

    /** Raggio delle ruote. */
    final float[] wheelRadius = { 0.3179f, 0.3179f, 0.3276f, 0.3276f };

    /** Parametri ABS: slip, range e velocità minima. */
    final float absSlip = 2.0f;
    final float absRange = 3.0f;
    final float absMinSpeed = 3.0f;

    /** Parametri per la gestione della frizione. */
    final float clutchMax = 0.5f;
    final float clutchDelta = 0.05f;
    final float clutchRange = 0.82f;
    final float clutchDeltaTime = 0.02f;
    final float clutchDeltaRaced = 10f;
    final float clutchDec = 0.01f;
    final float clutchMaxModifier = 1.3f;
    final float clutchMaxTime = 1.5f;

    /** Stato del blocco e valore corrente della frizione. */
    int stuck = 0;
    float clutch = 0;

    /** Costruttore di default. */
    public BaseDriver() {
        super();
    }

    /**
     * Calcola la marcia da utilizzare in base agli RPM.
     * @param sensors Modello dei sensori del veicolo.
     * @return Marcia consigliata.
     */
    int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();
        if (gear < 1) return 1;
        if (gear < 6 && rpm >= gearUp[gear - 1]) return gear + 1;
        if (gear > 1 && rpm <= gearDown[gear - 1]) return gear - 1;
        return gear;
    }

    /**
     * Calcola l'angolo di sterzata ottimale.
     * @param sensors Modello dei sensori del veicolo.
     * @return Valore normalizzato di sterzata.
     */
    float getSteer(SensorModel sensors) {
        float targetAngle = (float) (sensors.getAngleToTrackAxis() - sensors.getTrackPosition() * 0.5);
        if (sensors.getSpeed() > steerSensitivityOffset)
            return (float) (targetAngle / (steerLock * (sensors.getSpeed() - steerSensitivityOffset) * wheelSensitivityCoeff));
        else
            return targetAngle / steerLock;
    }

    /**
     * Calcola il livello di accelerazione desiderato.
     * @param sensors Modello dei sensori del veicolo.
     * @return Accelerazione normalizzata.
     */
    float getAccel(SensorModel sensors) {
        if (sensors.getTrackPosition() > -1 && sensors.getTrackPosition() < 1) {
            float rxSensor = (float) sensors.getTrackEdgeSensors()[10];
            float sensorsensor = (float) sensors.getTrackEdgeSensors()[9];
            float sxSensor = (float) sensors.getTrackEdgeSensors()[8];

            float targetSpeed;
            if (sensorsensor > maxSpeedDist || (sensorsensor >= rxSensor && sensorsensor >= sxSensor))
                targetSpeed = maxSpeed;
            else {
                float h = sensorsensor * sin5;
                float b = (Math.max(rxSensor, sxSensor)) - sensorsensor * cos5;
                float sinAngle = b * b / (h * h + b * b);
                targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
            }
            return (float) (2 / (1 + Math.exp(sensors.getSpeed() - targetSpeed)) - 1);
        } else {
            return 0.3f;
        }
    }

    /**
     * Gestisce automaticamente la frizione in fase di partenza.
     * @param sensors Modello dei sensori.
     * @param clutch Valore corrente della frizione.
     * @return Nuovo valore di frizione.
     */
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
                clutch = Math.max(0.0f, clutch);
            } else {
                clutch -= clutchDec;
            }
        }
        return clutch;
    }

    /**
     * Applica un filtro ABS per evitare il bloccaggio delle ruote.
     * @param sensors Modello dei sensori.
     * @param brake Valore corrente di frenata.
     * @return Valore corretto con ABS.
     */
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
        return Math.max(0, brake);
    }

    /**
     * Imposta l'accelerazione completa.
     * @param action Azione da modificare.
     * @param sensors Modello dei sensori.
     */
    void accelera(Action action, SensorModel sensors) {
        action.accelerate = 1.0;
        action.brake = 0.0;
        action.steering = 0f;
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
    }

    /**
     * Imposta la frenata completa.
     * @param action Azione da modificare.
     * @param sensors Modello dei sensori.
     */
    void frena(Action action, SensorModel sensors) {
        action.brake = filterABS(sensors, 1f);
        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
        action.accelerate = 0.0f;
    }

    /**
     * Imposta la retromarcia.
     * @param action Azione da modificare.
     * @param sensors Modello dei sensori.
     */
    void retromarcia(Action action, SensorModel sensors) {
        action.gear = -1;
        action.accelerate = 1.0f;
        action.brake = 0.0;
        action.clutch = clutchMax;
    }

    /**
     * Sterza verso sinistra.
     * @param action Azione da modificare.
     * @param sensors Modello dei sensori.
     */
    void giraSinistra(Action action, SensorModel sensors) {
        if (sensors.getSpeed() < 15)
            action.accelerate = 0.5;
        action.steering = 0.3f;
        action.brake = 0.0f;
        action.gear = sensors.getGear();
        action.clutch = 0.0f;
    }

    /**
     * Sterza verso destra.
     * @param action Azione da modificare.
     * @param sensors Modello dei sensori.
     */
    void giraDestra(Action action, SensorModel sensors) {
        if (sensors.getSpeed() < 15)
            action.accelerate = 0.5;
        action.steering = -0.3f;
        action.brake = 0.0f;
        action.gear = sensors.getGear();
        action.clutch = 0.0f;
    }

    /**
     * Decelerazione controllata.
     * @param action Azione da modificare.
     * @param sensors Modello dei sensori.
     */
    void decelera(Action action, SensorModel sensors) {
        action.steering = 0.0f;
        action.accelerate = 0.2f;
        action.brake = 0.0f;
        action.gear = sensors.getGear();
        action.clutch = 0.0f;
    }

    /**
     * Inizializza l'array di angoli dei sensori di bordo pista.
     * @return Array di 19 angoli da -90° a 90°.
     */
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

    /**
     * Messaggio al reset della simulazione.
     */
    @Override
    public void reset() {
        System.out.println("Restarting the race!");
    }

    /**
     * Messaggio alla chiusura della simulazione.
     */
    @Override
    public void shutdown() {
        System.out.println("Bye bye!");
    }
}
