package com.devteam.fantasy.message.response;

import java.util.List;

public class NumeroGanadorSorteoResponse {

	private String numero;
	
	private Long numeroGanadorId;
	
	private String sorteoType;
	
	private String hour;
	
	private String day;
	
	private double premio;
	
	private List<PairJP> jugadores;

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Long getNumeroGanadorId() {
		return numeroGanadorId;
	}

	public void setNumeroGanadorId(Long numeroGanadorId) {
		this.numeroGanadorId = numeroGanadorId;
	}

	public String getSorteoType() {
		return sorteoType;
	}

	public void setSorteoType(String sorteoType) {
		this.sorteoType = sorteoType;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public double getPremio() {
		return premio;
	}

	public void setPremio(double premio) {
		this.premio = premio;
	}

	public List<PairJP> getJugadores() {
		return jugadores;
	}

	public void setJugadores(List<PairJP> jugadores) {
		this.jugadores = jugadores;
	}
	
}
