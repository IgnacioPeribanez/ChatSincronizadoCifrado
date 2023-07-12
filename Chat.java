package practicaChat;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;

// Clase que actua como el constructor de un servidor Chat y controla sus acciones.
public class Chat {
	protected ServerSocket serverSocket;
	protected Socket clientSocket;
	protected ArrayList<Sala> salas = new ArrayList<Sala>();
	protected KeyPair claves;

	/*
	 * Pre: -- 
	 * Post: Este metodo gestiona las acciones del servidor Chat.
	 */
	public void startServer() {
			try {
				// Se lanza el servidor en el puerto 1234.
				serverSocket = new ServerSocket(1234);
				// Se asignan las claves para la codificacion de mensajes
				KeyPairGenerator generadorClaves = KeyPairGenerator.getInstance("RSA");
				generadorClaves.initialize(2048);
				claves = generadorClaves.generateKeyPair();
				while (true) {
					System.out.println("Esperando..."); 
					clientSocket = serverSocket.accept();
					System.out.println("Cliente en línea");
					// Se crea un hilo por cada conexion para manejar las peticiones del cliente.
	                ClientHandler clientSock = new ClientHandler(clientSocket, salas, claves);
	                new Thread(clientSock).start();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

}
