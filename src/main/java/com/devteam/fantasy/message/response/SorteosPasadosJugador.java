package com.devteam.fantasy.message.response;

public class SorteosPasadosJugador {

	private String id;
	
	private String username;
	
	private String name;
	
	private String bono;
	
	private String moneda;
	
	private String balance;

	public SorteosPasadosJugador() {
		super();
	}
	
	public SorteosPasadosJugador(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBono() {
		return bono;
	}

	public void setBono(String bono) {
		this.bono = bono;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SorteosPasadosJugador other = (SorteosPasadosJugador) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SorteosPasadosJugador [id=" + id + ", username=" + username + ", name=" + name + ", bono="
				+ bono + ", moneda=" + moneda + ", balance=" + balance + "]";
	}
}
