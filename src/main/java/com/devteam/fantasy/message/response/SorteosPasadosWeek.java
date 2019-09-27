package com.devteam.fantasy.message.response;

import java.util.List;
import java.util.Set;

public class SorteosPasadosWeek {

	private String ventas;
	
	private String comisiones;
	
	private String subTotal;
	
	private String premios;
	
	private String bonos;
	
	private String perdidasGanancias;

	private Set<SorteosPasadosJugador> jugadores;
	
	public String getVentas() {
		return ventas;
	}

	public void setVentas(String ventas) {
		this.ventas = ventas;
	}

	public String getComisiones() {
		return comisiones;
	}

	public void setComisiones(String comisiones) {
		this.comisiones = comisiones;
	}

	public String getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(String subTotal) {
		this.subTotal = subTotal;
	}

	public String getPremios() {
		return premios;
	}

	public void setPremios(String premios) {
		this.premios = premios;
	}

	public String getBonos() {
		return bonos;
	}

	public void setBonos(String bonos) {
		this.bonos = bonos;
	}

	public String getPerdidasGanancias() {
		return perdidasGanancias;
	}

	public void setPerdidasGanancias(String perdidasGanancias) {
		this.perdidasGanancias = perdidasGanancias;
	}
	
	public Set<SorteosPasadosJugador> getJugadores() {
		return jugadores;
	}

	public void setJugadores(Set<SorteosPasadosJugador> jugadores) {
		this.jugadores = jugadores;
	}

	@Override
	public String toString() {
		return "SorteosPasadosWeek [ventas=" + ventas + ", comisiones=" + comisiones + ", subTotal=" + subTotal
				+ ", premios=" + premios + ", bonos=" + bonos + ", perdidasGanancias=" + perdidasGanancias + "]";
	}
	
}