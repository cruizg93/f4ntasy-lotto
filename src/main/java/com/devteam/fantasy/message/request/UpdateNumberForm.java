package com.devteam.fantasy.message.request;

public class UpdateNumberForm {
    private Integer numero;

    private Double valor;


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
    public String toString() {
        return "UpdateNumberForm{" +
                "numero=" + numero +
                ", valor=" + valor +
                '}';
    }
}
