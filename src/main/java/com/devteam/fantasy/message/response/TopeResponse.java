package com.devteam.fantasy.message.response;

public class TopeResponse {

    private String numero;

    private Integer tope;

    public TopeResponse(String numero, Integer tope) {
        this.numero = numero;
        this.tope = tope;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getTope() {
        return tope;
    }

    public void setTope(Integer tope) {
        this.tope = tope;
    }
}
