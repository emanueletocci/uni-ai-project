package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.model.SensorModel;

/**
 * Abstract base class for implementing a TORCS car controller.
 * Provides the structure for control logic and race management.
 */
public abstract class Controller {

	/**
	 * Enumeration representing the different stages of a race.
	 */
	public enum Stage {

		/** Warm-up stage */
		WARMUP,

		/** Qualifying stage */
		QUALIFYING,

		/** Main race stage */
		RACE,

		/** Unknown or undefined stage */
		UNKNOWN;

		/**
		 * Converts an integer to the corresponding Stage.
		 *
		 * @param value the integer value representing the stage
		 * @return the corresponding Stage enum
		 */
		public static Stage fromInt(int value) {
			switch (value) {
				case 0:
					return WARMUP;
				case 1:
					return QUALIFYING;
				case 2:
					return RACE;
				default:
					return UNKNOWN;
			}
		}
	};

	private Stage stage;
	private String trackName;

	/**
	 * Initializes the range finder angles used for sensing the track.
	 *
	 * @return an array of 19 angles from -90 to +90 degrees in steps of 10
	 */
	public float[] initAngles() {
		float[] angles = new float[19];
		for (int i = 0; i < 19; ++i)
			angles[i] = -90 + i * 10;
		return angles;
	}

	/**
	 * Returns the current race stage.
	 *
	 * @return the current Stage
	 */
	public Stage getStage() {
		return stage;
	}

	/**
	 * Sets the current race stage.
	 *
	 * @param stage the Stage to set
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Returns the name of the current track.
	 *
	 * @return the track name
	 */
	public String getTrackName() {
		return trackName;
	}

	/**
	 * Sets the name of the track.
	 *
	 * @param trackName the name of the track
	 */
	public void setTrackName(String trackName) {
		this.trackName = trackName;
	}

	/**
	 * Main control method that must be implemented by subclasses.
	 * Defines the driving behavior given sensor inputs.
	 *
	 * @param sensors the current sensor readings
	 * @return an Action representing the desired driving commands
	 */
	public abstract Action control(SensorModel sensors);

	/**
	 * Called at the beginning of each new trial to reset internal state.
	 */
	public abstract void reset();

	/**
	 * Called once when the controller is shutting down.
	 * Used to clean up resources if necessary.
	 */
	public abstract void shutdown();

}
