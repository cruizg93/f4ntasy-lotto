package com.devteam.fantasy.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Bono {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne
    @JoinColumn
    private Week week;

	@ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

	private Double bono;
	
	@ManyToOne
    @JoinColumn(name = "moneda_id")
    private Moneda moneda;	
	
	@ManyToOne
    @JoinColumn(name = "cambio_id")
    Cambio cambio;

	@ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Week getWeek() {
		return week;
	}

	public void setWeek(Week week) {
		this.week = week;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Double getBono() {
		return bono;
	}

	public void setBono(Double bono) {
		this.bono = bono;
	}
	
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public Cambio getCambio() {
		return cambio;
	}

	public void setCambio(Cambio cambio) {
		this.cambio = cambio;
	}

	@Override
	public String toString() {
		return "Bono [id=" + id + ", week=" + week + ", user=" + user + ", bono=" + bono + "]";
	}
	
}


