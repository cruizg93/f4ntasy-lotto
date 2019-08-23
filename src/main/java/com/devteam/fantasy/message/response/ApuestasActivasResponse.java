package com.devteam.fantasy.message.response;

public class ApuestasActivasResponse {
    private Long id;
    private String title;
    private double total;
    private double comision;
    private double neta;
    private double premio;
    private double balance;
    private String estado;
    private String type;
    private String numeroGanador;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }

    public double getNeta() {
        return neta;
    }

    public void setNeta(double neta) {
        this.neta = neta;
    }

    public double getPremio() {
        return premio;
    }

    public void setPremio(double premio) {
        this.premio = premio;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumeroGanador() {
        return numeroGanador;
    }

    public void setNumeroGanador(String numeroGanador) {
        this.numeroGanador = numeroGanador;
    }
}
