package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.model.SensorModel;

public class SimpleDriver extends BaseDriver {

	@Override
	public void reset() {
		System.out.println("Restarting the race!");
	}

	@Override
	public void shutdown() {
		System.out.println("Bye bye!");
	}

	public Action control(SensorModel sensors) {
		// Controlla se l'auto è attualmente bloccata
		/**
			Se l'auto ha un angolo, rispetto alla traccia, superiore a 30°
			incrementa "stuck" che è una variabile che indica per quanti cicli l'auto è in
			condizione di difficoltà.
			Quando l'angolo si riduce, "stuck" viene riportata a 0 per indicare che l'auto è
			uscita dalla situaizone di difficoltà
		 **/
		if (Math.abs(sensors.getAngleToTrackAxis()) > stuckAngle) {
			// update stuck counter
			stuck++;
		} else {
			// if not stuck reset stuck counter
			stuck = 0;
		}

		// Applicare la polizza di recupero o meno in base al tempo trascorso
		/**
		Se "stuck" è superiore a 25 (stuckTime) allora procedi a entrare in situaizone di RECOVERY
		per far fronte alla situazione di difficoltà
		 **/

		if (stuck > stuckTime) { //Auto Bloccata
			/**
			 * Impostare la marcia e il comando di sterzata supponendo che l'auto stia puntando
			 * in una direzione al di fuori di pista
			 **/

			// Per portare la macchina parallela all'asse TrackPos
			float steer = (float) (-sensors.getAngleToTrackAxis() / steerLock);
			int gear = -1; // Retromarcia

			// Se l'auto è orientata nella direzione corretta invertire la marcia e sterzare
			if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
				gear = 1;
				steer = -steer;
			}
			clutch = clutching(sensors, clutch);
			// Costruire una variabile CarControl e restituirla
			Action action = new Action();
			action.gear = gear;
			action.steering = steer;
			action.accelerate = 1.0;
			action.brake = 0;
			action.clutch = clutch;
			return action;
		}

		else //Auto non Bloccata
		{
			// Calcolo del comando di accelerazione/frenata
			float accel_and_brake = getAccel(sensors);

			// Calcolare marcia da utilizzare
			int gear = getGear(sensors);

			// Calcolo angolo di sterzata
			float steer = getSteer(sensors);

			// Normalizzare lo sterzo
			if (steer < -1)
				steer = -1;
			if (steer > 1)
				steer = 1;

			// Impostare accelerazione e frenata dal comando congiunto accelerazione/freno
			float accel, brake;
			if (accel_and_brake > 0) {
				accel = accel_and_brake;
				brake = 0;
			} else {
				accel = 0;

				// Applicare l'ABS al freno
				brake = filterABS(sensors, -accel_and_brake);
			}
			clutch = clutching(sensors, clutch);

			// Costruire una variabile CarControl e restituirla
			Action action = new Action();
			action.gear = gear;
			action.steering = steer;
			action.accelerate = accel;
			action.brake = brake;
			action.clutch = clutch;
			return action;
		}
	}
}
