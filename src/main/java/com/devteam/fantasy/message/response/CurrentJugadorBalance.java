package com.devteam.fantasy.message.response;

public class CurrentJugadorBalance {

	private String moneda;
	
	private double balance;

	public CurrentJugadorBalance() {
		// TODO Auto-generated constructor stub
	}
	
	public CurrentJugadorBalance(String moneda, double balance) {
		super();
		this.moneda = moneda;
		this.balance = balance;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "CurrentJugadorBalance [moneda=" + moneda + ", balance=" + balance + "]";
	}
	
}
