package com.devteam.fantasy.message.response;

import java.util.List;

public class JugadorSorteosResponse {
    private String name;
    private String moneda;
    private List<SorteoResponse> sorteos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public List<SorteoResponse> getSorteos() {
        return sorteos;
    }

    public void setSorteos(List<SorteoResponse> sorteos) {
        this.sorteos = sorteos;
    }

}
