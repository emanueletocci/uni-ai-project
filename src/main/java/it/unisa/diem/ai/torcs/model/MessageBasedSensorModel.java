package it.unisa.diem.ai.torcs.model;

import it.unisa.diem.ai.torcs.io.MessageParser;

/**
 * Implementazione dell'interfaccia {@link SensorModel} basata su parsing di messaggi stringa.
 * Utilizza {@link MessageParser} per estrarre i valori dei sensori da una stringa in formato TORCS.
 *
 * Creato con IntelliJ IDEA.
 * Autore: Administrator
 * Data: 4 Marzo 2008
 * Ora: 15:44:29
 */
public class MessageBasedSensorModel implements SensorModel {

	private MessageParser message;

	/**
	 * Costruttore che riceve un oggetto {@link MessageParser} già costruito.
	 * @param message parser contenente i dati del messaggio
	 */
	public MessageBasedSensorModel(MessageParser message) {
		this.message = message;
	}

	/**
	 * Costruttore che costruisce internamente il parser partendo da una stringa.
	 * @param strMessage messaggio stringa ricevuto dal server TORCS
	 */
	public MessageBasedSensorModel(String strMessage) {
		this.message = new MessageParser(strMessage);
	}

	/** @return velocità longitudinale del veicolo (asse X) */
	public double getSpeed() {
		return (Double) message.getReading("speedX");
	}

	/** @return angolo tra l'asse del veicolo e l'asse della pista */
	public double getAngleToTrackAxis() {
		return (Double) message.getReading("angle");
	}

	/** @return array con i valori dei sensori di distanza ai bordi della pista */
	public double[] getTrackEdgeSensors() {
		return (double[]) message.getReading("track");
	}

	/** @return valori dei sensori di "focus" (direzione specifica osservata) */
	public double[] getFocusSensors() {
		return (double[]) message.getReading("focus");
	}

	/** @return marcia attualmente inserita (intero da -1 a 6) */
	public int getGear() {
		return (int) (double) (Double) message.getReading("gear");
	}

	/** @return array dei sensori di prossimità agli avversari (360°) */
	public double[] getOpponentSensors() {
		return (double[]) message.getReading("opponents");
	}

	/** @return posizione corrente in gara (1 = primo, ecc.) */
	public int getRacePosition() {
		return (int) (double) (Double) message.getReading("racePos");
	}

	/** @return velocità laterale del veicolo (asse Y) */
	public double getLateralSpeed() {
		return (Double) message.getReading("speedY");
	}

	/** @return tempo corrente sul giro in corso */
	public double getCurrentLapTime() {
		return (Double) message.getReading("curLapTime");
	}

	/** @return danno accumulato dal veicolo */
	public double getDamage() {
		return (Double) message.getReading("damage");
	}

	/** @return distanza dalla linea di partenza */
	public double getDistanceFromStartLine() {
		return (Double) message.getReading("distFromStart");
	}

	/** @return distanza totale percorsa in gara */
	public double getDistanceRaced() {
		return (Double) message.getReading("distRaced");
	}

	/** @return livello attuale di carburante nel serbatoio */
	public double getFuelLevel() {
		return (Double) message.getReading("fuel");
	}

	/** @return tempo dell'ultimo giro completato */
	public double getLastLapTime() {
		return (Double) message.getReading("lastLapTime");
	}

	/** @return numero di giri del motore al minuto */
	public double getRPM() {
		return (Double) message.getReading("rpm");
	}

	/** @return posizione del veicolo rispetto al centro pista (range [-1,1]) */
	public double getTrackPosition() {
		return (Double) message.getReading("trackPos");
	}

	/** @return velocità di rotazione delle quattro ruote (in rad/s) */
	public double[] getWheelSpinVelocity() {
		return (double[]) message.getReading("wheelSpinVel");
	}

	/** @return il messaggio grezzo originario ricevuto dal server */
	public String getMessage() {
		return message.getMessage();
	}

	/** @return altezza del veicolo dal suolo */
	public double getZ() {
		return (Double) message.getReading("z");
	}

	/** @return velocità verticale del veicolo */
	public double getZSpeed() {
		return (Double) message.getReading("speedZ");
	}
}
