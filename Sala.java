package practicaChat;

import java.util.ArrayList;

// Clase que actua como una sala donde pueden chatear diferentes clientes
public class Sala {
	protected String nombre;
	protected String identificador;
	protected String contrase�a;
	protected ArrayList<Usuario> online;

	public Sala(String nombre, String identificador, String contrase�a, ArrayList<Usuario> online) {
		this.nombre = nombre;
		this.identificador = identificador;
		this.contrase�a = contrase�a;
		this.online = online;
	}

	public Sala() {
		// TODO Auto-generated constructor stub
	}

	public String getNombre() {
		return nombre;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getIdentificador() {
		return identificador;
	}
	
	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}
	
	public String getContrase�a() {
		return contrase�a;
	}
	
	public void setContrase�a(String contrase�a) {
		this.contrase�a = contrase�a;
	}
	
	public ArrayList<Usuario> getOnline() {
		return online;
	}
	
	public void setOnline(ArrayList<Usuario> online) {
		this.online = online;
	}
	
}
