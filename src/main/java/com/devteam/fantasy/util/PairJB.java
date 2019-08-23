package com.devteam.fantasy.util;

public class PairJB {

    private Long id;
    private String nombre;
    private String moneda;
    private double balance;

    public String getNombre() {
        return nombre;
    }

    public PairJB() {
    }

    public PairJB(String nombre, double balance) {
        this.nombre = nombre;
        this.balance = balance;
    }

    public PairJB(String nombre, double balance,  String moneda) {
        this.nombre = nombre;
        this.moneda = moneda;
        this.balance = balance;
    }

    public PairJB(Long id, String nombre, double balance, String moneda) {
        this.id = id;
        this.nombre = nombre;
        this.moneda = moneda;
        this.balance = balance;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
