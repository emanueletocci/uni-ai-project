package it.unisa.diem.ai.torcs.model;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: Mar 4, 2008 Time: 3:35:31
 * PM
 */
public class Action {

	public double accelerate = 0; // 0..1
	public double brake = 0; // 0..1
	public double clutch = 0; // 0..1
	public int gear = 0; // -1..6
	public double steering = 0; // -1..1
	public boolean restartRace = false;
	public int focus = 360;	// ML Angolo di messa a fuoco desiderato in gradi [-90; 90],
							// impostare 360 se non si desidera alcuna lettura della messa a fuoco!

	public String toString() {
		limitValues();
		return "(accel " + accelerate + ") " + "(brake " + brake + ") " + "(clutch " + clutch + ") " + "(gear " + gear
				+ ") " + "(steer " + steering + ") " + "(meta " + (restartRace ? 1 : 0) + ") " + "(focus " + focus // ML
				+ ")";
	}

	public void limitValues() {
		accelerate = Math.max(0, Math.min(1, accelerate));
		brake = Math.max(0, Math.min(1, brake));
		clutch = Math.max(0, Math.min(1, clutch));
		steering = Math.max(-1, Math.min(1, steering));
		gear = Math.max(-1, Math.min(6, gear));

	}

	public void reset() {
		accelerate = 0;
		brake = 0;
		clutch = 0;
		gear = 0;
		steering = 0;
		restartRace = false;
		focus = 360; // ML
		limitValues();
	}
}
