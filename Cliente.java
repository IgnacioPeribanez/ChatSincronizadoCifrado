package practicaChat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

// Clase que actua como el constructor de un cliente y controla sus acciones.
public class Cliente {
	protected ServerSocket serverSocket;
	protected Socket clientSocket;
	protected ReaderHandler reader;
	protected WriterHandler writer;
	protected KeyPair claves;
	protected PublicKey keyPublicServ;

	/*
	 * Este semáforo gestiona la muestra de los mensajes.
	 */
	static Semaphore mostrar = new Semaphore(1);

	/*
	 * Pre: -- 
	 * Post: Este metodo lanza los Thread correspodientes para la gestion del chat.
	 */
	public void chat() {
		try {
			reader = new ReaderHandler(clientSocket, mostrar, claves.getPrivate());
			writer = new WriterHandler(clientSocket, mostrar, keyPublicServ);
			reader.start();
			writer.start();
			reader.join();
			writer.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Pre: -- 
	 * Post: Este metodo recibe la clave publica del servidor y envia al 
	 * 		 servidor la clave publica del cliente.
	 */
	public void compartirClaves(DataInputStream in, DataOutputStream out) {
		// Se convierte la clave a bytes para mandarla por el socket del servidor.
		byte[] bytesPublicaCli = claves.getPublic().getEncoded();
		try {
			// Se recibe la clave publica del servidor codificada con la clave publica del
			// servidor.
			byte[] bytesPublicaServ = new byte[in.readInt()];
			in.readFully(bytesPublicaServ);
			// Se almacena la clave publica del servidor
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytesPublicaServ);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			keyPublicServ = keyFactory.generatePublic(keySpec);
			// Opcion que codifica la clave publica para aportar mas seguridad.
//			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//			System.out.println("encrip gene");
//			rsaCipher.init(Cipher.ENCRYPT_MODE, publicServ);
//			System.out.println("encrip init");
//			byte[] publicaCli = rsaCipher.doFinal(claves.getPublic().getEncoded());
//			System.out.println("Clave encriptada");
			// Se envia la clave del cliente en froma de bytes.
			out.writeInt(bytesPublicaCli.length);
			out.write(bytesPublicaCli);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Pre: -- 
	 * Post: Este metodo gestiona las acciones de un cliente.
	 */
	public void startClient() {
		Scanner entrada = new Scanner(System.in);
		try {
			// Generamos claves cliente.
			KeyPairGenerator generadorClaves = KeyPairGenerator.getInstance("RSA");
			generadorClaves.initialize(2048);
			claves = generadorClaves.generateKeyPair();
			clientSocket = new Socket("localhost", 1234); // Socket para que el cliente se conecte al servidor del chat.
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			// Se comparten claves entre Cliente-Servidor.
			compartirClaves(in, out);
			String mensaje = in.readUTF();
			System.out.println(mensaje);
			while (true) {
				System.out.println(
						"---------------------------------------------MENU-----------------------------------------------------");
				System.out.println("1.- Unirse a una sala de chat"); // PostNote ["Juan", "Luis"]
				System.out.println("2.- Crear una nueva sala de chat"); // RemoveNote ["?A", "Luis"]
				System.out.println("3.- Listar las salas disponibles"); // ReadNote ["Juan", "?D"]
				System.out.print("Que accion deseas realizar?: ");
				int opcion = entrada.nextInt();
				out.writeInt(opcion);
				if (opcion == 1) {
					// Se pregunta la sala.
					String preguntaSala = in.readUTF();
					entrada.nextLine();
					while (true) {
						System.out.print(preguntaSala);
						String sala = entrada.nextLine();
						out.writeUTF(sala);
						if (in.readUTF().equalsIgnoreCase("stop")) {
							break;
						}
						System.out.println("Error, elija una opcion valida");
					}
					// Si es privada se pide contraseña.
					String privada = in.readUTF();
					if (privada.equalsIgnoreCase("Esta sala es privada, cual es la contraseña: ")) {
						System.out.print(privada);
						String contraseña = entrada.nextLine();
						out.writeUTF(contraseña);
					}
					// Si no hay ningun error el usuario entra en la sala.
					String conexion = in.readUTF();
					System.out.println(conexion);
					if (conexion.equalsIgnoreCase("Entrando en la sala...")) {
						chat();
					}
				} else if (opcion == 2) {
					// Pregunta el tipo de sala que quiere crear el cliente.
					String preguntaTipo = in.readUTF();
					entrada.nextLine();
					String tipo = "";
					while (true) {
						System.out.println(preguntaTipo);
						tipo = entrada.nextLine();
						if (tipo.equalsIgnoreCase("publica") || tipo.equalsIgnoreCase("privada")) {
							out.writeUTF(tipo);
							break;
						} else {
							System.out.println("Error, elija una opcion valida");
						}
					}
					// Pregunta el nomnbre de la sala.
					System.out.print(in.readUTF());
					String newNombre = entrada.nextLine();
					out.writeUTF(newNombre);
					// Si el usuario elige crear una sala privada se pide la contraseña
					if (tipo.equalsIgnoreCase("privada")) {
						// Pregunta contraseña
						mensaje = in.readUTF();
						System.out.print(mensaje);
						String newPassword = entrada.nextLine();
						out.writeUTF(newPassword);
					}
					System.out.println("Entrando en la sala...");
					chat();
				} else if (opcion == 3) {
					// Se muestran las salas
					int i = 1;
					while (true) {
						String sala = in.readUTF();
						if (!sala.equalsIgnoreCase("stop")) {
							System.out.println("Sala " + i + ": " + sala);
							i++;
						} else {
							break;
						}
					}
				} else {
					System.out.println("Error, elija una opcion valida");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}