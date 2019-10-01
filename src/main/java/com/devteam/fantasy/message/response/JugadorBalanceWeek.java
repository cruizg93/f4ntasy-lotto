package com.devteam.fantasy.message.response;

public class JugadorBalanceWeek {

	private Long id;
	private String name;
	private String username;
	private boolean haveBono;
	private double balance;
	private String moneda;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public boolean isHaveBono() {
		return haveBono;
	}
	public void setHaveBono(boolean haveBono) {
		this.haveBono = haveBono;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public String getMoneda() {
		return moneda;
	}
	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}
	@Override
	public String toString() {
		return "JugadorBalanceWeek [id=" + id + ", name=" + name + ", username=" + username + ", haveBono=" + haveBono
				+ ", balance=" + balance + ", moneda=" + moneda + "]";
	}
	
	 
}
