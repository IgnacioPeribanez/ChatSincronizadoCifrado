package practicaChat;

import java.io.IOException;

// Clase principal que hará la funcion de lanzar el chat
public class MainServidor {
	
	public static void main(String[] args) throws IOException {
		Chat chat = new Chat();
		chat.startServer();
	}
	
}