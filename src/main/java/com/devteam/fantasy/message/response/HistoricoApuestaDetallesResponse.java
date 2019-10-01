package com.devteam.fantasy.message.response;

import com.devteam.fantasy.util.PairNV;

import java.util.List;

public class HistoricoApuestaDetallesResponse {
    private Long userId;
    private String title;
    private List<PairNV> apuestas;
    private int total;
    private String moneda;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PairNV> getApuestas() {
        return apuestas;
    }

    public void setApuestas(List<PairNV> apuestas) {
        this.apuestas = apuestas;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
}
