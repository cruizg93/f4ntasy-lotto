package com.devteam.fantasy.message.request;

import javax.validation.constraints.NotNull;

public class CambioForm {

    @NotNull
    private Double cambio;

    public Double getCambio() {
        return cambio;
    }

    public void setCambio(Double cambio) {
        this.cambio = cambio;
    }
}
