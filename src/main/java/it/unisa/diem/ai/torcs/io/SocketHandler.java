/**
 * Gestore della comunicazione via socket UDP con il simulatore TORCS.
 * Si occupa di inviare e ricevere pacchetti UDP da/verso il server.
 * Utilizza {@link DatagramSocket} per la comunicazione.
 *
 * Autore: Daniele Loiacono
 */
package it.unisa.diem.ai.torcs.io;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class SocketHandler {

	private InetAddress address;     // Indirizzo IP del server
	private final int port;                // Porta UDP del server
	private DatagramSocket socket;   // Socket UDP usato per comunicare
	private final boolean verbose;         // Modalità verbose (log attivi)

	/**
	 * Costruttore del SocketHandler.
	 *
	 * @param host indirizzo del server TORCS (es. "localhost")
	 * @param port porta UDP del server (default: 3001)
	 * @param verbose se true, stampa tutti i messaggi inviati/ricevuti
	 */
	public SocketHandler(String host, int port, boolean verbose) {

		// Imposta l'indirizzo remoto
		try {
			this.address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.port = port;

		// Inizializza il socket UDP
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.verbose = verbose;
	}

	/**
	 * Invia un messaggio stringa al server.
	 *
	 * @param msg il messaggio da inviare
	 */
	public void send(String msg) {
		if (verbose)
			System.out.println("Sending: " + msg);
		try {
			byte[] buffer = msg.getBytes();
			socket.send(new DatagramPacket(buffer, buffer.length, address, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Riceve un messaggio dal server (bloccante, senza timeout).
	 *
	 * @return il messaggio ricevuto, oppure null in caso di errore
	 */
	public String receive() {
		try {
			byte[] buffer = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			String received = new String(packet.getData(), 0, packet.getLength());
			if (verbose)
				System.out.println("Received: " + received);
			return received;
		} catch (SocketTimeoutException se) {
			if (verbose)
				System.out.println("Socket Timeout!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Riceve un messaggio dal server con timeout specificato (in millisecondi).
	 *
	 * @param timeout durata massima dell'attesa (ms)
	 * @return il messaggio ricevuto, oppure null se scade il timeout
	 */
	public String receive(int timeout) {
		try {
			socket.setSoTimeout(timeout); // imposta timeout
			String received = receive();  // chiama receive() normale
			socket.setSoTimeout(0);       // resetta il timeout (bloccante)
			return received;
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Chiude il socket e termina la comunicazione.
	 */
	public void close() {
		socket.close();
	}
}
