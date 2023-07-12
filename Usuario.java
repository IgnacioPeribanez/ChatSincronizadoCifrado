package practicaChat;

import java.net.Socket;
import java.security.PublicKey;

// Clase que actua como el constructor de un usuario.
public class Usuario {
	protected Socket socket;
	protected PublicKey publicKey;
	
	public Socket getSocket() {
		return socket;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
	}
	
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	
	public Usuario(Socket socket, PublicKey publicKey) {
		this.socket = socket;
		this.publicKey = publicKey;
	}

}