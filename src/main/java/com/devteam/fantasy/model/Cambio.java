package com.devteam.fantasy.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;


@Entity
@Table(name = "cambio")
public class Cambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Double cambio;

    @NotNull
    private Timestamp cambioTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getCambio() {
        return cambio;
    }

    public void setCambio(Double cambio) {
        this.cambio = cambio;
    }

    public Timestamp getCambioTime() {
        return cambioTime;
    }

    public void setCambioTime(Timestamp cambioTime) {
        this.cambioTime = cambioTime;
    }

	@Override
	public String toString() {
		return "Cambio [cambio=" + cambio + ", cambioTime=" + cambioTime + "]";
	}
    
    
}
