package com.devteam.fantasy.message.request;

public class BonoRequest {

	private Long weekId;
	
	private double bono;
	
	private String moneda;
	
	public Long getWeekId() {
		return weekId;
	}

	public void setWeekId(Long weekId) {
		this.weekId = weekId;
	}

	public double getBono() {
		return bono;
	}

	public void setBono(double bono) {
		this.bono = bono;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}
	
}
