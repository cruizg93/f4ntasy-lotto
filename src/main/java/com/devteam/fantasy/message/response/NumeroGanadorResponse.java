package com.devteam.fantasy.message.response;

import com.devteam.fantasy.util.PairJB;

import java.util.List;

public class NumeroGanadorResponse {
    private Long id;
    private String title;
    private Integer numero;
    private List<PairJB> pairJBS;


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

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public List<PairJB> getPairJBS() {
        return pairJBS;
    }

    public void setPairJBS(List<PairJB> pairJBS) {
        this.pairJBS = pairJBS;
    }
}
