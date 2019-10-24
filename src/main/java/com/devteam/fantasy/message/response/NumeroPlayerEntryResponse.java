package com.devteam.fantasy.message.response;

public class NumeroPlayerEntryResponse {

    private String numero;

    private Integer tope;

    private Integer max;

    private Double current;

    private boolean noFirst;

    public NumeroPlayerEntryResponse() {
    }

    public NumeroPlayerEntryResponse(String numero, Integer tope, Integer max) {
        this.numero = numero;
        this.tope = tope;
        this.max = max;
        this.current=0.0;
    }

    public NumeroPlayerEntryResponse(String numero, Integer tope, Integer max, Double current) {
        this.numero = numero;
        this.tope = tope;
        this.max = max;
        this.current = current;
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

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Double getCurrent() {
        return current;
    }

    public void setCurrent(Double current) {
        this.current = current;
    }

    public boolean isNoFirst() {
        return noFirst;
    }

    public void setNoFirst(boolean noFirst) {
        this.noFirst = noFirst;
    }

    @Override
    public String toString() {
        return "NumeroPlayerEntryResponse{numero='" + numero + ", current=" + current +'}';
    }
}
