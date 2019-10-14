package com.devteam.fantasy.message.response;

import com.devteam.fantasy.model.Moneda;

public class JugadorDataResponse {
    private Long id;
    private String username;
    private String name;
    private Moneda moneda;
    
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
    
    private String chicaType;
    private String diariaType;

    private double balance;
    private boolean Editable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEditable() {
        return Editable;
    }

    public void setEditable(boolean editable) {
        Editable = editable;
    }

	public String getChicaType() {
		return chicaType;
	}

	public void setChicaType(String chicaType) {
		this.chicaType = chicaType;
	}

	public String getDiariaType() {
		return diariaType;
	}

	public void setDiariaType(String diariaType) {
		this.diariaType = diariaType;
	}
}
