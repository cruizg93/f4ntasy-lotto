package com.devteam.fantasy.util;

public class PairNV implements Comparable<PairNV>{

    private Integer numero;
    private Double valor;

    public PairNV() {
    }

    public PairNV(Integer numero, Double valor) {
        this.numero = numero;
        this.valor = valor;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    @Override
    public int compareTo(PairNV o) {
        return (this.numero).compareTo(o.numero);
    }

}
