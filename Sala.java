package practicaChat;

import java.util.ArrayList;

// Clase que actua como una sala donde pueden chatear diferentes clientes
public class Sala {
	protected String nombre;
	protected String identificador;
	protected String contraseña;
	protected ArrayList<Usuario> online;

	public Sala(String nombre, String identificador, String contraseña, ArrayList<Usuario> online) {
		this.nombre = nombre;
		this.identificador = identificador;
		this.contraseña = contraseña;
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
	
	public String getContraseña() {
		return contraseña;
	}
	
	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}
	
	public ArrayList<Usuario> getOnline() {
		return online;
	}
	
	public void setOnline(ArrayList<Usuario> online) {
		this.online = online;
	}
	
}
