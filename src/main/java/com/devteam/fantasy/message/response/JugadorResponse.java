package com.devteam.fantasy.message.response;

import java.util.List;

public class JugadorResponse {
    private Long id;
    private String username;
    private String name;
    private String monedaType;
    private Double balance;
    private Double total;
    private Double comision;
    private Double riesgo;
    private List<AsistenteResponse> asistentes;

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

    public String getMonedaType() {
        return monedaType;
    }

    public void setMonedaType(String monedaType) {
        this.monedaType = monedaType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getComision() {
        return comision;
    }

    public void setComision(Double comision) {
        this.comision = comision;
    }

    public Double getRiesgo() {
        return riesgo;
    }

    public void setRiesgo(Double riesgo) {
        this.riesgo = riesgo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AsistenteResponse> getAsistentes() {
        return asistentes;
    }

    public void setAsistentes(List<AsistenteResponse> asistentes) {
        this.asistentes = asistentes;
    }
}
