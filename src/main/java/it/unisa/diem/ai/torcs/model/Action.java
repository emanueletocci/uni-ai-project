package it.unisa.diem.ai.torcs.model;

/**
 * Rappresenta un'azione di controllo da inviare al simulatore TORCS.
 * Contiene tutti i comandi del veicolo: accelerazione, frenata, sterzo, cambio, frizione, ecc.
 *
 * Creato con IntelliJ IDEA.
 * Autore: Administrator
 * Data: 4 Marzo 2008
 * Ora: 15:35:31
 */
public class Action {

	/** Valore di accelerazione (range: 0..1) */
	public double accelerate = 0;

	/** Valore di frenata (range: 0..1) */
	public double brake = 0;

	/** Valore della frizione (range: 0..1) */
	public double clutch = 0;

	/** Marcia inserita (range: -1 = retromarcia, 0 = folle, 1..6 = marce) */
	public int gear = 0;

	/** Valore di sterzo (range: -1 = sinistra massima, 0 = dritto, 1 = destra massima) */
	public double steering = 0;

	/** Se true, richiede il riavvio della gara */
	public boolean restartRace = false;

	/**
	 * Angolo di messa a fuoco per i sensori opzionali (in gradi, range [-90;90]).
	 * Impostare 360 per disattivare la messa a fuoco.
	 */
	public int focus = 360;

	/**
	 * Converte l'azione in stringa nel formato richiesto dal protocollo TORCS.
	 *
	 * @return una stringa rappresentante l'azione da inviare al simulatore
	 */
	public String toString() {
		limitValues();
		return "(accel " + accelerate + ") " +
				"(brake " + brake + ") " +
				"(clutch " + clutch + ") " +
				"(gear " + gear + ") " +
				"(steer " + steering + ") " +
				"(meta " + (restartRace ? 1 : 0) + ") " +
				"(focus " + focus + ")";
	}

	/**
	 * Applica dei limiti ai valori di controllo per evitare comandi non validi.
	 * Garantisce che tutti i valori siano nel range previsto.
	 */
	public void limitValues() {
		accelerate = Math.max(0, Math.min(1, accelerate));
		brake = Math.max(0, Math.min(1, brake));
		clutch = Math.max(0, Math.min(1, clutch));
		steering = Math.max(-1, Math.min(1, steering));
		gear = Math.max(-1, Math.min(6, gear));
	}

	/**
	 * Reimposta l'azione a valori di default (tutti i comandi disattivati).
	 * Utilizzato ad esempio all'inizio di un episodio.
	 */
	public void reset() {
		accelerate = 0;
		brake = 0;
		clutch = 0;
		gear = 0;
		steering = 0;
		restartRace = false;
		focus = 360; // Disabilita focus
		limitValues();
	}
}
