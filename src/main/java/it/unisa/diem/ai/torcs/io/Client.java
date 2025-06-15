/**
 * Client di connessione per il simulatore TORCS.
 * Si occupa di gestire la comunicazione tra l'agente e il simulatore,
 * l'inizializzazione dei parametri, l'invio dei comandi e la ricezione dei sensori.
 *
 * Opzioni configurabili tramite args:
 * - port:N -> porta di comunicazione (default 3001)
 * - host:IP -> indirizzo host (default localhost)
 * - id:nome -> identificativo del client (default SCR)
 * - verbose:on -> attiva log dettagliati
 * - maxEpisodes:N -> numero massimo di episodi (default 1)
 * - maxSteps:N -> numero massimo di step per episodio (default 0 = illimitati)
 * - stage:N -> stadio (0 = WARMUP, 1 = QUALIFYING, 2 = RACE, altri = UNKNOWN)
 * - trackName:nome -> nome della pista
 *
 * @author Daniele Loiacono
 */
package it.unisa.diem.ai.torcs.io;

import java.util.StringTokenizer;

import it.unisa.diem.ai.torcs.agent.Controller;
import it.unisa.diem.ai.torcs.agent.Controller.Stage;
import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.model.MessageBasedSensorModel;

public class Client {

	private static int UDP_TIMEOUT = 10000;
	private static int port;
	private static String host;
	private static String clientId;
	private static boolean verbose;
	private static int maxEpisodes;
	private static int maxSteps;
	private static Stage stage;
	private static String trackName;

	/**
	 * Metodo principale che avvia la simulazione.
	 *
	 * @param args argomenti da riga di comando per configurare la simulazione
	 */
	public static void main(String[] args) {

		parseParameters(args); // Analizza i parametri della riga di comando

		SocketHandler mySocket = new SocketHandler(host, port, verbose);
		String inMsg;

		Controller driver = load(args[0]); // Carica dinamicamente il controller
		driver.setStage(stage);
		driver.setTrackName(trackName);

		// Costruisce la stringa di inizializzazione (angoli radar)
		float[] angles = driver.initAngles();
		String initStr = clientId + "(init";
		for (int i = 0; i < angles.length; i++) {
			initStr = initStr + " " + angles[i];
		}
		initStr = initStr + ")";

		long curEpisode = 0;
		boolean shutdownOccurred = false;

		do {
			// Fase di identificazione con il server TORCS
			do {
				mySocket.send(initStr);
				inMsg = mySocket.receive(UDP_TIMEOUT);
			} while (inMsg == null || inMsg.indexOf("***identified***") < 0);

			// Inizio dell'episodio di guida
			long currStep = 0;
			while (true) {
				inMsg = mySocket.receive(UDP_TIMEOUT);

				if (inMsg != null) {

					// Verifica se la simulazione è terminata
					if (inMsg.indexOf("***shutdown***") >= 0) {
						shutdownOccurred = true;
						System.out.println("Server shutdown!");
						break;
					}

					// Verifica se la simulazione è stata riavviata
					if (inMsg.indexOf("***restart***") >= 0) {
						driver.reset();
						if (verbose)
							System.out.println("Server restarting!");
						break;
					}

					Action action = new Action();

					// Controllo dell'agente solo se non si è superato maxSteps
					if (currStep < maxSteps || maxSteps == 0)
						action = driver.control(new MessageBasedSensorModel(inMsg));
					else
						action.restartRace = true;

					currStep++;
					mySocket.send(action.toString());

				} else {
					System.out.println("Server did not respond within the timeout");
				}
			}

		} while (++curEpisode < maxEpisodes && !shutdownOccurred);

		// Chiusura del client e del controller
		driver.shutdown();
		mySocket.close();
		System.out.println("Client shutdown.");
		System.out.println("Bye, bye!");
	}

	/**
	 * Analizza i parametri passati da riga di comando e imposta le opzioni globali.
	 *
	 * @param args array di stringhe con i parametri in formato chiave:valore
	 */
	private static void parseParameters(String[] args) {
		port = 3001;
		host = "localhost";
		clientId = "SCR";
		verbose = false;
		maxEpisodes = 1;
		maxSteps = 0;
		stage = Stage.UNKNOWN;
		trackName = "unknown";

		for (int i = 1; i < args.length; i++) {
			StringTokenizer st = new StringTokenizer(args[i], ":");
			String entity = st.nextToken();
			String value = st.nextToken();

			if (entity.equals("port")) {
				port = Integer.parseInt(value);
			}
			if (entity.equals("host")) {
				host = value;
			}
			if (entity.equals("id")) {
				clientId = value;
			}
			if (entity.equals("verbose")) {
				if (value.equals("on"))
					verbose = true;
				else if (value.equals("off"))
					verbose = false;
				else {
					System.out.println(entity + ":" + value + " is not a valid option");
					System.exit(0);
				}
			}
			if (entity.equals("stage")) {
				stage = Stage.fromInt(Integer.parseInt(value));
			}
			if (entity.equals("trackName")) {
				trackName = value;
			}
			if (entity.equals("maxEpisodes")) {
				maxEpisodes = Integer.parseInt(value);
				if (maxEpisodes <= 0) {
					System.out.println(entity + ":" + value + " is not a valid option");
					System.exit(0);
				}
			}
			if (entity.equals("maxSteps")) {
				maxSteps = Integer.parseInt(value);
				if (maxSteps < 0) {
					System.out.println(entity + ":" + value + " is not a valid option");
					System.exit(0);
				}
			}
		}
	}

	/**
	 * Carica dinamicamente una classe Controller a partire dal nome fornito.
	 *
	 * @param name nome della classe da caricare
	 * @return un'istanza del controller
	 */
	private static Controller load(String name) {
		Controller controller = null;
		try {
			controller = (Controller) (Object) Class.forName(name).newInstance();
		} catch (ClassNotFoundException e) {
			System.out.println(name + " is not a class name");
			System.exit(0);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return controller;
	}
}
