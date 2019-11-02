package com.devteam.fantasy.model;

import javax.persistence.*;

@Entity
public class NumeroGanador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "sorteos_id", referencedColumnName = "id")
    private Sorteo sorteo;

    private Integer numeroGanador;

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

    public Integer getNumeroGanador() {
        return numeroGanador;
    }

    public void setNumeroGanador(Integer numeroGanador) {
        this.numeroGanador = numeroGanador;
    }

	@Override
	public String toString() {
		return "NumeroGanador [id=" + id + ", numeroGanador=" + numeroGanador + ", sorteo=" + sorteo + "]";
	}
    
    
}
