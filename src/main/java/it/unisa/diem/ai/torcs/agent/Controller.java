package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.model.SensorModel;

/**
 * Classe astratta base per l'implementazione di un controller per TORCS.
 * Fornisce la struttura per la logica di guida e la gestione della gara.
 */
public abstract class Controller {

	/**
	 * Enumerazione che rappresenta i diversi stadi di una gara.
	 */
	public enum Stage {

		/** Fase di riscaldamento (warm-up) */
		WARMUP,

		/** Fase di qualifica */
		QUALIFYING,

		/** Fase di gara principale */
		RACE,

		/** Stato sconosciuto o non definito */
		UNKNOWN;

		/**
		 * Converte un intero nello stadio corrispondente.
		 *
		 * @param value valore intero che rappresenta lo stadio
		 * @return lo stadio corrispondente come enumerazione Stage
		 */
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

	/**
	 * Inizializza gli angoli dei sensori di distanza (range finder) utilizzati per percepire la pista.
	 *
	 * @return un array di 19 angoli da -90 a +90 gradi con passi di 10
	 */
	public float[] initAngles() {
		float[] angles = new float[19];
		for (int i = 0; i < 19; ++i)
			angles[i] = -90 + i * 10;
		return angles;
	}

	/**
	 * Restituisce lo stadio corrente della gara.
	 *
	 * @return lo stadio attuale (Stage)
	 */
	public Stage getStage() {
		return stage;
	}

	/**
	 * Imposta lo stadio corrente della gara.
	 *
	 * @param stage lo stadio da impostare
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Restituisce il nome della pista corrente.
	 *
	 * @return il nome della pista
	 */
	public String getTrackName() {
		return trackName;
	}

	/**
	 * Imposta il nome della pista.
	 *
	 * @param trackName il nome della pista
	 */
	public void setTrackName(String trackName) {
		this.trackName = trackName;
	}

	/**
	 * Metodo principale di controllo da implementare nelle sottoclassi.
	 * Definisce il comportamento del veicolo dato l'input dei sensori.
	 *
	 * @param sensors letture correnti dei sensori
	 * @return un oggetto Action che rappresenta i comandi di guida desiderati
	 */
	public abstract Action control(SensorModel sensors);

	/**
	 * Chiamato all'inizio di ogni nuova prova per reimpostare lo stato interno.
	 */
	public abstract void reset();

	/**
	 * Chiamato una sola volta alla chiusura del controller.
	 * Può essere usato per liberare risorse se necessario.
	 */
	public abstract void shutdown();
}
