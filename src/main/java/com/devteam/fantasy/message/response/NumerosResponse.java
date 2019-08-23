package com.devteam.fantasy.message.response;

public class NumerosResponse {

    private Integer numero;

    private Integer tope;


    public NumerosResponse(Integer numero, Integer tope) {
        this.numero = numero;
        this.tope = tope;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getTope() {
        return tope;
    }

    public void setTope(Integer tope) {
        this.tope = tope;
    }
}
