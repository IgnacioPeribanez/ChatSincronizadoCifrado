package practicaChat;

import java.io.IOException;

// Clase principal que hará uso del cliente
public class MainCliente {

	public static void main(String[] args) throws IOException {
		Cliente cli = new Cliente(); // Se crea el cliente
		cli.startClient(); // Se inicia el cliente
	}

}