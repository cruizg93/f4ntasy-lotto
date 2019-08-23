package com.devteam.fantasy.model;


import javax.persistence.*;

@Entity
public class Jugador extends User {

    @ManyToOne
    @JoinColumn
    private Moneda moneda;

    @ManyToOne
    @JoinColumn
    private TipoApostador tipoApostador;

    @ManyToOne
    @JoinColumn
    private TipoChica tipoChica;


    private double comisionChicaDirecto;
    private double premioChicaDirecto;

    private double costoChicaMiles;
    private double premioChicaMiles;

    private double comisionChicaPedazos;
    private double costoChicaPedazos;
    private double premioChicaPedazos;

    private double comisionDirecto;
    private double premioDirecto;

    private double costoMil;
    private double premioMil;

    private double balance;

    public Jugador() {
    }

    public Jugador(Moneda moneda, TipoApostador tipoApostador, TipoChica tipoChica) {
        this.moneda = moneda;
        this.tipoApostador = tipoApostador;
        this.tipoChica = tipoChica;
    }

    public Jugador(String name, String username, String password,
                   Moneda moneda,
                   TipoApostador tipoApostador,
                   TipoChica tipoChica) {
        super(name, username, password);
        this.moneda = moneda;
        this.tipoApostador = tipoApostador;
        this.tipoChica = tipoChica;
    }

    public Jugador(String name, String username, String password,
                   Moneda moneda,
                   TipoApostador tipoApostador,
                   TipoChica tipoChica,
                   double comisionChicaDirecto,
                   double premioChicaDirecto,
                   double costoChicaMiles,
                   double premioChicaMiles,
                   double comisionChicaPedazos,
                   double costoChicaPedazos,
                   double premioChicaPedazos,
                   double comisionDirecto,
                   double premioDirecto,
                   double costoMil,
                   double premioMil,
                   double balance) {
        super(name, username, password);
        this.moneda = moneda;
        this.tipoApostador = tipoApostador;
        this.tipoChica = tipoChica;
        this.comisionChicaDirecto = comisionChicaDirecto;
        this.premioChicaDirecto = premioChicaDirecto;
        this.costoChicaMiles = costoChicaMiles;
        this.premioChicaMiles = premioChicaMiles;
        this.comisionChicaPedazos = comisionChicaPedazos;
        this.costoChicaPedazos = costoChicaPedazos;
        this.premioChicaPedazos = premioChicaPedazos;
        this.comisionDirecto = comisionDirecto;
        this.premioDirecto = premioDirecto;
        this.costoMil = costoMil;
        this.premioMil = premioMil;
        this.balance = balance;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public TipoApostador getTipoApostador() {
        return tipoApostador;
    }

    public void setTipoApostador(TipoApostador tipoApostador) {
        this.tipoApostador = tipoApostador;
    }

    public TipoChica getTipoChica() {
        return tipoChica;
    }

    public void setTipoChica(TipoChica tipoChica) {
        this.tipoChica = tipoChica;
    }

    public double getComisionChicaDirecto() {
        return comisionChicaDirecto;
    }

    public void setComisionChicaDirecto(double comisionChicaDirecto) {
        this.comisionChicaDirecto = comisionChicaDirecto;
    }

    public double getPremioChicaDirecto() {
        return premioChicaDirecto;
    }

    public void setPremioChicaDirecto(double premioChicaDirecto) {
        this.premioChicaDirecto = premioChicaDirecto;
    }

    public double getCostoChicaMiles() {
        return costoChicaMiles;
    }

    public void setCostoChicaMiles(double costoChicaMiles) {
        this.costoChicaMiles = costoChicaMiles;
    }

    public double getPremioChicaMiles() {
        return premioChicaMiles;
    }

    public void setPremioChicaMiles(double premioChicaMiles) {
        this.premioChicaMiles = premioChicaMiles;
    }

    public double getComisionChicaPedazos() {
        return comisionChicaPedazos;
    }

    public void setComisionChicaPedazos(double comisionChicaPedazos) {
        this.comisionChicaPedazos = comisionChicaPedazos;
    }

    public double getCostoChicaPedazos() {
        return costoChicaPedazos;
    }

    public void setCostoChicaPedazos(double costoChicaPedazos) {
        this.costoChicaPedazos = costoChicaPedazos;
    }

    public double getPremioChicaPedazos() {
        return premioChicaPedazos;
    }

    public void setPremioChicaPedazos(double premioChicaPedazos) {
        this.premioChicaPedazos = premioChicaPedazos;
    }

    public double getComisionDirecto() {
        return comisionDirecto;
    }

    public void setComisionDirecto(double comisionDirecto) {
        this.comisionDirecto = comisionDirecto;
    }

    public double getPremioDirecto() {
        return premioDirecto;
    }

    public void setPremioDirecto(double premioDirecto) {
        this.premioDirecto = premioDirecto;
    }

    public double getCostoMil() {
        return costoMil;
    }

    public void setCostoMil(double costoMil) {
        this.costoMil = costoMil;
    }

    public double getPremioMil() {
        return premioMil;
    }

    public void setPremioMil(double premioMil) {
        this.premioMil = premioMil;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }


}
