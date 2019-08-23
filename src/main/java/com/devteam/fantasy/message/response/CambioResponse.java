package com.devteam.fantasy.message.response;

public class CambioResponse {

    private Double cambio;

    public CambioResponse(Double cambio) {
        this.cambio = cambio;
    }

    public Double getCambio() {
        return cambio;
    }

    public void setCambio(Double cambio) {
        this.cambio = cambio;
    }
}
