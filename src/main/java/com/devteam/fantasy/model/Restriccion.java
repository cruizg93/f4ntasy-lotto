package com.devteam.fantasy.model;


import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "restricciones")
public class Restriccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Integer numero;

    private Integer puntos;

    private Integer puntosCurrent;

    private Timestamp timestamp;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getPuntosCurrent() {
        return puntosCurrent;
    }

    public void setPuntosCurrent(Integer puntosCurrent) {
        this.puntosCurrent = puntosCurrent;
    }


}
