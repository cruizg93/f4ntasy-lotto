package com.devteam.fantasy.message.response;

import java.util.List;

public class SorteosPasadosJugadores extends SorteosPasados {

	private List<JugadorBalanceWeek> jugadores;

	public List<JugadorBalanceWeek> getJugadores() {
		return jugadores;
	}

	public void setJugadores(List<JugadorBalanceWeek> jugadores) {
		this.jugadores = jugadores;
	}

}