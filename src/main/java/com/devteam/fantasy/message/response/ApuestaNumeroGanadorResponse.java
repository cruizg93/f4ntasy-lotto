package com.devteam.fantasy.message.response;

public class ApuestaNumeroGanadorResponse {
    private String title;
    private Integer numero;
    private String status;
    private Long sorteId;
    private String type;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSorteId() {
        return sorteId;
    }

    public void setSorteId(Long sorteId) {
        this.sorteId = sorteId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
