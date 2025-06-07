package it.unisa.diem.ai.torcs.sensors;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: Mar 4, 2008 Time:
 * 12:18:47 PM
 */
public interface SensorModel {


	// Informazioni di base sull'auto e sul tracciato

	public double getSpeed();

	public double getAngleToTrackAxis();

	public double[] getTrackEdgeSensors();

	public double[] getFocusSensors();// ML

	public double getTrackPosition();

	public int getGear();

	// informazioni di base sulle altre auto (utili solo per le gare con più auto)

	public double[] getOpponentSensors();

	public int getRacePosition();

	// informazioni aggiuntive (utilizzare se necessario)

	public double getLateralSpeed();

	public double getCurrentLapTime();

	public double getDamage();

	public double getDistanceFromStartLine();

	public double getDistanceRaced();

	public double getFuelLevel();

	public double getLastLapTime();

	public double getRPM();

	public double[] getWheelSpinVelocity();

	public double getZSpeed();

	public double getZ();

	public String getMessage();

}
