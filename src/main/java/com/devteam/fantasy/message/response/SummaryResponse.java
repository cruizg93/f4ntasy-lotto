package com.devteam.fantasy.message.response;

public class SummaryResponse {

	private double ventas;
	
	private double comisiones;
	
	private double subTotal;
	
	private double premios;
	
	private double bonos;
	
	private double perdidasGanas;
	
	private String currency;

	public double getVentas() {
		return ventas;
	}

	public void setVentas(double ventas) {
		this.ventas = ventas;
	}

	public double getComisiones() {
		return comisiones;
	}

	public void setComisiones(double comisiones) {
		this.comisiones = comisiones;
	}

	public double getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	public double getPremios() {
		return premios;
	}

	public void setPremios(double premios) {
		this.premios = premios;
	}

	public double getBonos() {
		return bonos;
	}

	public void setBonos(double bonos) {
		this.bonos = bonos;
	}

	public double getPerdidasGanas() {
		if( perdidasGanas == 0) {
			perdidasGanas = subTotal - premios - bonos;
		}
		
		return perdidasGanas;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public String toString() {
		return "SummaryResponse [ventas=" + ventas + ", comisiones=" + comisiones + ", subTotal=" + subTotal
				+ ", premios=" + premios + ", bonos=" + bonos + ", perdidasGanas=" + perdidasGanas + ", currency="
				+ currency + "]";
	}

}
