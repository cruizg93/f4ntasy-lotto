package com.devteam.fantasy.model;

import java.sql.Timestamp;

import javax.persistence.*;
@Entity
public class HistoricoApuestas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sorteos_id")
    private Sorteo sorteo;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "asistente_id")
    private User asistente;

    @ManyToOne
    @JoinColumn(name = "cambio_id")
    Cambio cambio;

    private int numero;

    private double cantidad;

    private Timestamp date;
    
    private double cantidadMultiplier;
    
    private double premioMultiplier;
    
    private double comisionMultiplier;
    
    private String moneda;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sorteo getSorteo() {
        return sorteo;
    }

    public void setSorteo(Sorteo sorteo) {
        this.sorteo = sorteo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public Cambio getCambio() {
        return cambio;
    }

    public void setCambio(Cambio cambio) {
        this.cambio = cambio;
    }

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public double getCantidadMultiplier() {
		return cantidadMultiplier;
	}

	public void setCantidadMultiplier(double cantidadMultiplier) {
		this.cantidadMultiplier = cantidadMultiplier;
	}

	public double getPremioMultiplier() {
		return premioMultiplier;
	}

	public void setPremioMultiplier(double premioMultiplier) {
		this.premioMultiplier = premioMultiplier;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}
	
	public User getAsistente() {
		return asistente;
	}

	public void setAsistente(User asistente) {
		this.asistente = asistente;
	}

	@Override
	public String toString() {
		return "HistoricoApuestas [id=" + id + ", sorteo=" + sorteo + ", user=" + user + ", asistente=" + asistente
				+ ", cambio=" + cambio + ", numero=" + numero + ", cantidad=" + cantidad 
				+ ", date=" + date + ", cantidadMultiplier=" + cantidadMultiplier + ", premioMultiplier="
				+ premioMultiplier + ", moneda=" + moneda + "]";
	}

	public double getComisionMultiplier() {
		return comisionMultiplier;
	}

	public void setComisionMultiplier(double comisionMultiplier) {
		this.comisionMultiplier = comisionMultiplier;
	}
}
