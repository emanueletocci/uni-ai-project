package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.model.SensorModel;

/**
 * A very basic implementation of the {@link Controller} class.
 * This controller drives the car with a constant acceleration and very simple
 * steering logic.
 * <p>
 * Designed mainly for demonstration or testing purposes.
 * </p>
 *
 * @author
 * Created by IntelliJ IDEA.
 * Date: Mar 4, 2008
 * Time: 4:59:21 PM
 */
public class DeadSimpleSoloController extends Controller {

	/** The target speed in units per second. */
	final double targetSpeed = 15;

	/**
	 * Main control method which defines the driving behavior based on sensor input.
	 * <ul>
	 * <li>Accelerates if current speed is below the target speed</li>
	 * <li>Steers slightly based on the angle to the track axis</li>
	 * <li>Sets gear to 1</li>
	 * </ul>
	 *
	 * @param sensorModel the current sensor data from the car
	 * @return an {@link Action} representing the control commands
	 */
	@Override
	public Action control(SensorModel sensorModel) {
		Action action = new Action();

		if (sensorModel.getSpeed() < targetSpeed) {
			action.accelerate = 1;
		}

		if (sensorModel.getAngleToTrackAxis() < 0) {
			action.steering = -0.1;
		} else {
			action.steering = 0.1;
		}

		action.gear = 1;
		return action;
	}

	/**
	 * Resets the internal state before a new trial starts.
	 * Simply prints a message to the console.
	 */
	@Override
	public void reset() {
		System.out.println("Restarting the race!");
	}

	/**
	 * Called when the controller is shutting down.
	 * Simply prints a message to the console.
	 */
	@Override
	public void shutdown() {
		System.out.println("Bye bye!");
	}
}
