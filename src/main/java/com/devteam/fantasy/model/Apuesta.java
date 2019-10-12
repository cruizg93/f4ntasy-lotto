package com.devteam.fantasy.model;


import java.sql.Timestamp;

import javax.persistence.*;

@Entity
@Table(name= "apuestas")
public class Apuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sorteos_activos_id")
    SorteoDiaria sorteoDiaria;

    @ManyToOne
    @JoinColumn(name = "users_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "cambio_id")
    Cambio cambio;

    Timestamp date;
    
    private Integer numero;

    private Double cantidad;

    public Apuesta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SorteoDiaria getSorteoDiaria() {
        return sorteoDiaria;
    }

    public void setSorteoDiaria(SorteoDiaria sorteoDiaria) {
        this.sorteoDiaria = sorteoDiaria;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
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

	@Override
	public String toString() {
		return "Apuesta [id=" + id + ", cantidad=" + cantidad + ", numero=" + numero
				+ ", sorteoDiaria=" + sorteoDiaria + "]";
	}

}
