package com.devteam.fantasy.message.response;

import java.util.List;

public class JugadoresResponse {

	List<JugadorResponse> jugadores;
	
	double totalLempira;
	
	double totalDolar;

	public List<JugadorResponse> getJugadores() {
		return jugadores;
	}

	public void setJugadores(List<JugadorResponse> jugadores) {
		this.jugadores = jugadores;
	}

	public double getTotalLempira() {
		return totalLempira;
	}

	public void setTotalLempira(double totalLempira) {
		this.totalLempira = totalLempira;
	}

	public double getTotalDolar() {
		return totalDolar;
	}

	public void setTotalDolar(double totalDolar) {
		this.totalDolar = totalDolar;
	}

	@Override
	public String toString() {
		return "JugadoresResponse [jugadores.size()=" + jugadores.size() + ", totalLempira=" + totalLempira + ", totalDolar="
				+ totalDolar + "]";
	}
	
}
