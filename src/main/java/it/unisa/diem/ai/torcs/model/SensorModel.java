package it.unisa.diem.ai.torcs.model;

/**
 * Interfaccia che definisce il contratto per l’accesso ai dati dei sensori in TORCS.
 * Implementata da classi che forniscono letture sensoriali in tempo reale dal simulatore.
 * I sensori includono informazioni sul veicolo, sul tracciato, sugli avversari e su altri parametri dinamici.
 *
 * @author
 */
public interface SensorModel {

	// === INFORMAZIONI BASE SUL VEICOLO E TRACCIATO ===

	/**
	 * @return la velocità longitudinale del veicolo (asse X) in km/h.
	 */
    double getSpeed();

	/**
	 * @return l’angolo (in radianti) tra l’asse longitudinale del veicolo e l’asse centrale della pista.
	 */
    double getAngleToTrackAxis();

	/**
	 * @return un array di 19 valori che rappresentano la distanza dai bordi della pista a varie angolazioni (da -90° a +90°).
	 */
    double[] getTrackEdgeSensors();

	/**
	 * @return un array di 5 valori che rappresentano le letture dei sensori di messa a fuoco direzionale.
	 */
    double[] getFocusSensors();

	/**
	 * @return la posizione laterale del veicolo rispetto al centro della pista, in un intervallo [-1, 1].
	 */
    double getTrackPosition();

	/**
	 * @return la marcia attualmente inserita (-1 = retromarcia, 0 = folle, 1–6 = marce avanti).
	 */
    int getGear();


	// === INFORMAZIONI SUGLI AVVERSARI (SOLO GARE MULTI-AUTO) ===

	/**
	 * @return un array di 36 valori che indicano la distanza dagli avversari in ogni direzione (risoluzione 10°).
	 */
    double[] getOpponentSensors();

	/**
	 * @return la posizione corrente del veicolo nella gara (es. 1 = primo).
	 */
    int getRacePosition();


	// === INFORMAZIONI ADDIZIONALI ===

	/**
	 * @return la velocità laterale del veicolo (asse Y) in km/h.
	 */
    double getLateralSpeed();

	/**
	 * @return il tempo corrente del giro in corso, in secondi.
	 */
    double getCurrentLapTime();

	/**
	 * @return il livello di danni subiti dal veicolo (valore cumulativo).
	 */
    double getDamage();

	/**
	 * @return la distanza dalla linea di partenza lungo la pista, in metri.
	 */
    double getDistanceFromStartLine();

	/**
	 * @return la distanza totale percorsa in gara fino a questo momento, in metri.
	 */
    double getDistanceRaced();

	/**
	 * @return il livello corrente del carburante nel serbatoio, in litri.
	 */
    double getFuelLevel();

	/**
	 * @return la durata del giro precedente, in secondi.
	 */
    double getLastLapTime();

	/**
	 * @return i giri al minuto (RPM) attuali del motore.
	 */
    double getRPM();

	/**
	 * @return un array con 4 valori che rappresentano la velocità di rotazione di ciascuna ruota.
	 */
    double[] getWheelSpinVelocity();

	/**
	 * @return la velocità verticale del veicolo (asse Z), in km/h.
	 */
    double getZSpeed();

	/**
	 * @return la posizione verticale del veicolo (altezza sul piano), in metri.
	 */
    double getZ();

	/**
	 * @return il messaggio grezzo completo ricevuto dal simulatore (per debug/logging).
	 */
    String getMessage();
}
