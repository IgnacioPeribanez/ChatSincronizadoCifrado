package practicaChat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import javax.crypto.Cipher;

// Clase que actua como un controlador para el envio de mensajes del cliente.
public class WriterHandler extends Thread {
	private Socket clientSocket;
	private Semaphore mostrar;
	protected PublicKey publicServ;

	public WriterHandler(Socket clientSocket, Semaphore mostrar, PublicKey publicServ) {
		this.clientSocket = clientSocket;
		this.mostrar = mostrar;
		this.publicServ = publicServ;
	}
	
	@Override
	public void run() {
		Scanner entrada = new Scanner(System.in);
		try {
			System.out.println("Writer alive");
			// Se crea el codificador.
			Cipher encriptadorRSA = Cipher.getInstance("RSA");
			encriptadorRSA.init(Cipher.ENCRYPT_MODE, publicServ);
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			while (true) {
				// Se codifica el mensaje y se envia al chat, este se encarga de distribuirlo.
				String mensaje = entrada.nextLine();
				if (mensaje.length() <= 140) {
					byte[] mensajeCifrado = encriptadorRSA.doFinal(mensaje.getBytes("UTF8"));
					mostrar.acquire();
					if (!mensaje.equalsIgnoreCase("/disconnect")) {
						out.writeInt(mensajeCifrado.length);
						out.write(mensajeCifrado);
						mostrar.release();
					} else {
						System.out.println("Writer die");
						out.writeInt(mensajeCifrado.length);
						out.write(mensajeCifrado);
						mostrar.release();
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
