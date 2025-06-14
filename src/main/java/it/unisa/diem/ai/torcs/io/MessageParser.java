package it.unisa.diem.ai.torcs.io;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Classe per il parsing dei messaggi ricevuti dal server TORCS.
 * Estrae le letture dei sensori dal messaggio e le organizza in una tabella (nome -> valore).
 * Supporta sia valori singoli che array (es. track, opponents, wheelSpinVel, focus).
 * <p>
 * Esempio di messaggio:
 * "(speedX 45.0)(angle 0.01)(track -1.0 -1.0 ...)"
 * </p>
 *
 * Creato con IntelliJ IDEA.
 * Autore: Administrator
 * Data: 22 Febbraio 2008
 * Ora: 18:17:32
 */
public class MessageParser {

	/** Tabella contenente le letture del messaggio (nome -> valore o array di valori) */
	private final Hashtable<String, Object> table = new Hashtable<>();

	/** Messaggio originale ricevuto dal server */
	private final String message;

	/**
	 * Costruttore che esegue direttamente il parsing del messaggio.
	 *
	 * @param message stringa contenente il messaggio completo dal server
	 */
	public MessageParser(String message) {
		this.message = message;

		// Tokenizza il messaggio in base all'apertura della parentesi
		StringTokenizer mt = new StringTokenizer(message, "(");
		while (mt.hasMoreElements()) {
			// Estrae una lettura
			String reading = mt.nextToken();
			int endOfMessage = reading.indexOf(")");
			if (endOfMessage > 0) {
				reading = reading.substring(0, endOfMessage);
			}

			// Tokenizza la lettura in base agli spazi: primo token è il nome, il resto sono i valori
			StringTokenizer rt = new StringTokenizer(reading, " ");
			if (rt.countTokens() < 2) {
				// Lettura non valida (ignorata)
			} else {
				String readingName = rt.nextToken();
				Object readingValue = "";

				// Se la lettura è un array (es. track, opponents...), inizializza array di double
				if (readingName.equals("opponents") || readingName.equals("track") || readingName.equals("wheelSpinVel")
						|| readingName.equals("focus")) {
					readingValue = new double[rt.countTokens()];
					int position = 0;

					while (rt.hasMoreElements()) {
						String nextToken = rt.nextToken();
						try {
							((double[]) readingValue)[position] = Double.parseDouble(nextToken);
						} catch (Exception e) {
							System.out.println("Error parsing value '" + nextToken + "' for " + readingName + " using 0.0");
							System.out.println("Message: " + message);
							((double[]) readingValue)[position] = 0.0;
						}
						position++;
					}
				} else {
					// Lettura scalare (valore singolo)
					String token = rt.nextToken();
					try {
						readingValue = new Double(token);
					} catch (Exception e) {
						System.out.println("Error parsing value '" + token + "' for " + readingName + " using 0.0");
						System.out.println("Message: " + message);
						readingValue = new Double(0.0);
					}
				}

				// Inserisce nella tabella (nome -> valore)
				table.put(readingName, readingValue);
			}
		}
	}

	/**
	 * Stampa tutte le letture presenti nella tabella.
	 */
	public void printAll() {
		Enumeration<String> keys = table.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			System.out.print(key + ":  ");
			System.out.println(table.get(key));
		}
	}

	/**
	 * Restituisce il valore associato a una lettura (es. "speedX", "track", ecc.).
	 *
	 * @param key nome della lettura
	 * @return valore della lettura (Double o double[]), oppure null se non esiste
	 */
	public Object getReading(String key) {
		return table.get(key);
	}

	/**
	 * Restituisce il messaggio originale grezzo ricevuto dal server.
	 *
	 * @return il messaggio stringa originale
	 */
	public String getMessage() {
		return message;
	}
}
