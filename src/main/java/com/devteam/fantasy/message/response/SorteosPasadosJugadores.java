package com.devteam.fantasy.message.response;

import java.util.Set;

public class SorteosPasadosJugadores extends SorteosPasados {

	private Set<JugadorBalanceWeek> jugadores;

	public Set<JugadorBalanceWeek> getJugadores() {
		return jugadores;
	}

	public void setJugadores(Set<JugadorBalanceWeek> jugadores) {
		this.jugadores = jugadores;
	}

}