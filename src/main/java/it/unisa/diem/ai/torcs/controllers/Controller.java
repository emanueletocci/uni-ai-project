package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.sensors.SensorModel;

public abstract class Controller {
	    final int[] gearUp = {7500, 8000, 8500, 9000, 9500, 0};
    final int[] gearDown = {0, 2800, 3200, 4800, 5200, 5000};

    /* Costanti del filtro ABS */
    final float wheelRadius[] = {(float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276};
    final float absSlip = (float) 2.0;
    final float absRange = (float) 3.0;
    final float absMinSpeed = (float) 3.0;

    /* Costanti da stringere */
    final float clutchMax = (float) 0.5;
    final float clutchDelta = (float) 0.05;
    final float clutchRange = (float) 0.82;
    final float clutchDeltaTime = (float) 0.02;
    final float clutchDeltaRaced = 10;
    final float clutchDec = (float) 0.01;
    final float clutchMaxModifier = (float) 1.3;
    final float clutchMaxTime = (float) 1.5;

	    private int stuck = 0;

    // current clutch
    private float clutch = 0;
	    protected int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        // Se la marcia è 0 (N) o -1 (R) restituisce semplicemente 1
        if (gear < 1)
            return 1;

        // Se il valore di RPM dell'auto è maggiore di quello suggerito
        // sale di marcia rispetto a quella attuale
        if (gear < 6 && rpm >= gearUp[gear - 1]) {
            return gear + 1;
        } else
            if (gear > 1 && rpm <= gearDown[gear - 1]) {
                return gear - 1;
            } else {
                return gear;
            }
    }

	public enum Stage {

		WARMUP, QUALIFYING, RACE, UNKNOWN;

		public static Stage fromInt(int value) {
            return switch (value) {
                case 0 -> WARMUP;
                case 1 -> QUALIFYING;
                case 2 -> RACE;
                default -> UNKNOWN;
            };
		}
	}

    private Stage stage;
	private String trackName;

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public String getTrackName() {
		return trackName;
	}

	public void setTrackName(String trackName) {
		this.trackName = trackName;
	}

	    protected float filterABS(SensorModel sensors, float brake) {
        // Converte la velocità in m/s
        float speed = (float) (sensors.getSpeed() / 3.6);

        // Quando la velocità è inferiore alla velocità minima per l'abs non interviene in caso di frenata
        if (speed < absMinSpeed)
            return brake;

        // Calcola la velocità delle ruote in m/s
        float slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
        }

        // Lo slittamento è la differenza tra la velocità effettiva dell'auto e la velocità media delle ruote
        slip = speed - slip / 4.0f;

        // Quando lo slittamento è troppo elevato, si applica l'ABS
        if (slip > absSlip) {
            brake = brake - (slip - absSlip) / absRange;
        }

        // Controlla che il freno non sia negativo, altrimenti lo imposta a zero
        if (brake < 0)
            return 0;
        else
            return brake;
    }

    protected float clutching(SensorModel sensors, float clutch) {

        float maxClutch = clutchMax;

        // Controlla se la situazione attuale è l'inizio della gara
        if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE
                && sensors.getDistanceRaced() < clutchDeltaRaced)
            clutch = maxClutch;

        // Regolare il valore attuale della frizione
        if (clutch > 0) {
            double delta = clutchDelta;
            if (sensors.getGear() < 2) {

                // Applicare un'uscita più forte della frizione quando la marcia è una e la corsa è appena iniziata.
                delta /= 2;
                maxClutch *= clutchMaxModifier;
                if (sensors.getCurrentLapTime() < clutchMaxTime)
                    clutch = maxClutch;
            }

            // Controllare che la frizione non sia più grande dei valori massimi
            clutch = Math.min(maxClutch, clutch);

            // Se la frizione non è al massimo valore, diminuisce abbastanza rapidamente
            if (clutch != maxClutch) {
                clutch -= delta;
                clutch = Math.max((float) 0.0, clutch);
            }
            // Se la frizione è al valore massimo, diminuirla molto lentamente.
            else
                clutch -= clutchDec;
        }
        return clutch;
    }

    public float[] initAngles() {

        float[] angles = new float[19];

        /*
         * set angles as
         * {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90}
         */
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

	public abstract Action control(SensorModel sensors);

	public abstract void reset(); // called at the beginning of each new trial

	public abstract void shutdown();

}