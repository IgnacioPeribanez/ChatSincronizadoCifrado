package practicaChat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

// Clase que actua como un controlador para las peciciones de un cliente sobre el Chat.
public class ClientHandler implements Runnable {
	private Socket clientSocket;
	private ArrayList<Sala> salas;
	protected KeyPair claves;

	public ClientHandler(Socket clientSocket, ArrayList<Sala> salas, KeyPair claves) {
		this.clientSocket = clientSocket;
		this.salas = salas;
		this.claves = claves;
	}
	
	/*
	 * Pre: -- 
	 * Post: Este metodo genera un identificador alphanumerico de 6 digitos.
	 */
	public String generateApha() {
		int leftLimit = 48;
		int rightLimit = 122;
		Random random = new SecureRandom();
		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(6)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
		System.out.println(generatedString);
		return generatedString;
	}

	/*
	 * Pre: -- 
	 * Post: Este metodo gestiona el chat de un cliente.
	 */
	public void chat(DataInputStream in, DataOutputStream out, Sala salaSelecciona, Usuario usuario) {
		try {
			// Se crea el cifrador RSA.
			Cipher cifradorRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cifradorRSA.init(Cipher.DECRYPT_MODE, claves.getPrivate());
			// Se añade el cliente a los online de la sala.
			salaSelecciona.getOnline().add(usuario);
			for (int i = 0; i < salaSelecciona.getOnline().size(); i++) {
				System.out.println("Hay " + (i + 1) + " usuarios conectados a la sala " + salaSelecciona.getNombre());
			}
			while (true) {
				// Se recibe el mensaje del cliente en forma de bytes.
				byte[] mensajeCifrado = new byte[in.readInt()];
				in.readFully(mensajeCifrado);
				// Se descifra con la clave privada del servidor.
				byte[] mensajeDescifradoBytes = cifradorRSA.doFinal(mensajeCifrado);
				String mensajeDescifrado = new String(mensajeDescifradoBytes, "UTF8");
				System.out.println("Sala " + salaSelecciona.getNombre() + ": " + mensajeDescifrado);
				// Si el cliente se quiere desconectar de la sala
				if (mensajeDescifrado.equalsIgnoreCase("/disconnect")) {
					// Se cifra el mensaje y se envia al reader del cliente para que finalice.
					cifradorRSA.init(Cipher.ENCRYPT_MODE, usuario.getPublicKey());
					byte[] envioCifrado = cifradorRSA.doFinal(mensajeDescifrado.getBytes("UTF8"));
					for (int i = 0; i < salaSelecciona.getOnline().size(); i++) {
						// Se saca el cliente de los online.
						if (salaSelecciona.getOnline().get(i).getSocket() == clientSocket) {
							salaSelecciona.getOnline().remove(i);
							break;
						}
					}
					out.writeInt(envioCifrado.length);
					out.write(envioCifrado);
					break;
				}
				// Se envia el mensaje cifrado a todos los clientes de la sala, cifrado con 
				// la clave publica de cada cliente
				for (int i = 0; i < salaSelecciona.getOnline().size(); i++) {
					if (salaSelecciona.getOnline().get(i).getSocket() != clientSocket) {
						Cipher cifradorRSABucle = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						cifradorRSABucle.init(Cipher.ENCRYPT_MODE, salaSelecciona.getOnline().get(i).getPublicKey());
						byte[] envioCifrado = cifradorRSABucle.doFinal(mensajeDescifrado.getBytes("UTF8"));
						new DataOutputStream(salaSelecciona.getOnline().get(i).getSocket().getOutputStream())
								.writeInt(envioCifrado.length);
						new DataOutputStream(salaSelecciona.getOnline().get(i).getSocket().getOutputStream())
						.write(envioCifrado);
						System.out.println("Mensaje enviado");
					}
				}
			}
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Pre: -- 
	 * Post: Este metodo recibe la clave publica del cliente, envia al 
	 * 		 cliente la clave publica del servidor y devuelve el usuario del cliente
	 */
	public Usuario compartirClaves(DataInputStream in, DataOutputStream out) {
		byte[] bytesPublicaServ = claves.getPublic().getEncoded();
		PublicKey publicKey = null;
		try {
			// Se envia la clave publica del servidor
			out.writeInt(bytesPublicaServ.length);
			out.write(bytesPublicaServ);
			System.out.println("Enviada la clave");
			// Se recibe la clave del cliente
			byte[] bytesPublicaCli = new byte[in.readInt()];
			in.readFully(bytesPublicaCli);
			System.out.println("Clave recibida");
			// Almacenamos la clave publica del cliente
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytesPublicaCli);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKey = keyFactory.generatePublic(keySpec);
			System.out.println("Clave almacenada");
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Usuario(clientSocket, publicKey);
	}

	@Override
	public void run() {
		try {
			Sala salaSelecciona = new Sala();
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			Usuario usuario = compartirClaves(in, out);
			out.writeUTF("BIENVENIDO AL SERVICIO CHAT");
			while (true) {
				int opcion = in.readInt();
				boolean existe = false;
				// Opcion de unirse a sala.
				if (opcion == 1) {
					boolean permiso = false;
					out.writeUTF("¿A que sala quiere unirse? ");
					while (true) {
						String peticion = in.readUTF();
						System.out.println(peticion);
						for (int i = 0; i < salas.size(); i++) {
							if (peticion.equalsIgnoreCase(
									salas.get(i).getNombre() + "#" + salas.get(i).getIdentificador())) {
								salaSelecciona = salas.get(i);
								existe = true;
							}
						}
						if (existe == true) {
							out.writeUTF("stop");
							System.out.println("stop");
							break;
						} else {
							out.writeUTF("continue");
						}
					}
					if (salaSelecciona.getContraseña() != null) {
						System.out.println("Privada");
						out.writeUTF("Esta sala es privada, cual es la contraseña: ");
						String contraseña = in.readUTF();
						if (contraseña.equals(salaSelecciona.getContraseña())) {
							permiso = true;
							System.out.println("Permiso");
							out.writeUTF("Entrando en la sala...");
							chat(in, out, salaSelecciona, usuario);
						} else {
							out.writeUTF("Contraseña incorrecta");
						}
					} else {
						out.writeUTF("publica");
						System.out.println("Permiso publica");
						out.writeUTF("Entrando en la sala...");
						chat(in, out, salaSelecciona, usuario);
					}
				// Opcion de crear una sala.
				} else if (opcion == 2) {
					out.writeUTF("¿La sala sera publica o privada?");
					String tipo = in.readUTF();
					out.writeUTF("Introduce un nombre: ");
					String newNombre = in.readUTF();
					if (tipo.equalsIgnoreCase("Privada")) {
						out.writeUTF("Introduce una contraseña: ");
						String newPassword = in.readUTF();
						salaSelecciona = new Sala(newNombre, generateApha(), newPassword, new ArrayList<Usuario>());
						salas.add(salaSelecciona);
					} else {
						salaSelecciona = new Sala(newNombre, generateApha(), null, new ArrayList<Usuario>());
						salas.add(salaSelecciona);
					}
					chat(in, out, salaSelecciona, usuario);
				// Opcion de mostrar las salas.
				} else if (opcion == 3) {
					for (int i = 0; i < salas.size(); i++) {
						if (salas.get(i).getContraseña() == null) {
							out.writeUTF(salas.get(i).getNombre() + "#" + salas.get(i).getIdentificador());
						} else {
							out.writeUTF(salas.get(i).getNombre() + "#" + salas.get(i).getIdentificador()
									+ ", contraseña: " + salas.get(i).getContraseña());
						}
					}
					out.writeUTF("stop");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
