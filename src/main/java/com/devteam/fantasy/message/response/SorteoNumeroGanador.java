package com.devteam.fantasy.message.response;

public class SorteoNumeroGanador {

	private String id;
	
	private String type;
	
	private String hour;
	
	private String numero;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	@Override
	public String toString() {
		return "SorteoNumeroGanador [id=" + id + ", type=" + type + ", hour=" + hour + ", numero=" + numero + "]";
	}


}
