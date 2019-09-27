package com.devteam.fantasy.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NaturalId;

import com.devteam.fantasy.util.BalanceType;
import com.devteam.fantasy.util.MonedaName;

@Entity
public class HistoricoBalance {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
	
	double balanceSemana;
	
	@Enumerated(EnumType.STRING)
    @Column(length = 60)
    private BalanceType balanceType;
	
	/*
	 * If balance Type is weekly, sorteoTime should be sunday 9 pm
	 * otherwise input sorteo time
	 */
	@Column(name="sorteo_time")
	Timestamp sorteoTime;
	
	@ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;

	@ManyToOne
    @JoinColumn(name = "moneda_id")
    private Moneda moneda;	
	
	@ManyToOne
    @JoinColumn(nullable = true)
    private Week week;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public double getBalanceSemana() {
		return balanceSemana;
	}

	public void setBalanceSemana(double balanceSemana) {
		this.balanceSemana = balanceSemana;
	}


	public Jugador getJugador() {
		return jugador;
	}

	public void setJugador(Jugador jugador) {
		this.jugador = jugador;
	}
	
	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public BalanceType getBalanceType() {
		return balanceType;
	}

	public void setBalanceType(BalanceType balanceType) {
		this.balanceType = balanceType;
	}

	public Timestamp getSorteoTime() {
		return sorteoTime;
	}

	public void setSorteoTime(Timestamp sorteoTime) {
		this.sorteoTime = sorteoTime;
	}

	public Week getWeek() {
		return week;
	}

	public void setWeek(Week week) {
		this.week = week;
	}

	@Override
	public String toString() {
		return "HistoricoBalance [id=" + id + ", createdBy=" + createdBy + ", balanceSemana=" + balanceSemana
				+ ", balanceType=" + balanceType.toString() + ", sorteoTime=" + sorteoTime + ", jugador=" + jugador.getName() + ", moneda="
				+ moneda.getMonedaName().toString() + "]";
	}

	
}
