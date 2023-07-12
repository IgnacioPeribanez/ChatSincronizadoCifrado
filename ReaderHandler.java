package practicaChat;

import java.io.DataInputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.concurrent.Semaphore;

import javax.crypto.Cipher;

// Clase que actua como un controlador para los mensajes recibidos del chat.
public class ReaderHandler extends Thread {
	private Socket clientSocket;
	private Semaphore mostrar;
	protected PrivateKey privateClient;
	
	public ReaderHandler(Socket clientSocket, Semaphore mostrar, PrivateKey privateClient) {
		this.clientSocket = clientSocket;
		this.mostrar = mostrar;
		this.privateClient = privateClient;
	}

	@Override
	public void run() {
		try {
			System.out.println("Reader alive");
			// Se crea el descifrador de mensajes.
			Cipher cifradorRSA = Cipher.getInstance("RSA");
			cifradorRSA.init(Cipher.DECRYPT_MODE, privateClient);
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			while (true) {
				// Se lee el mensaje, se descifra y se muestra por pantalla
				byte[] mensajeCifrado = new byte[in.readInt()];
				in.readFully(mensajeCifrado);
				byte[] mensajeDescifradoBytes = cifradorRSA.doFinal(mensajeCifrado);
				String mensajeDescifrado = new String(mensajeDescifradoBytes, "UTF8");
				mostrar.acquire();
				// Opcion de desconectar
				if (!mensajeDescifrado.equalsIgnoreCase("/disconnect")) {
					System.out.println(mensajeDescifrado);
					mostrar.release();
				} else {
					System.out.println("Reader Die");
					mostrar.release();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
