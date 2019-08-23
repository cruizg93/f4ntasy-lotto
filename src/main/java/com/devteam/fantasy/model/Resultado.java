package com.devteam.fantasy.model;

import javax.persistence.*;

@Entity
@Table(name= "resultado")
public class Resultado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sorteos_id")
    Sorteo sorteo;

    @ManyToOne
    @JoinColumn(name = "users_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "cambio_id")
    Cambio cambio;

    double cantApuesta;

    double comision;

    double premio;

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

    public double getCantApuesta() {
        return cantApuesta;
    }

    public void setCantApuesta(double cantApuesta) {
        this.cantApuesta = cantApuesta;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }

    public double getPremio() {
        return premio;
    }

    public void setPremio(double premio) {
        this.premio = premio;
    }

    public Cambio getCambio() {
        return cambio;
    }

    public void setCambio(Cambio cambio) {
        this.cambio = cambio;
    }
}
