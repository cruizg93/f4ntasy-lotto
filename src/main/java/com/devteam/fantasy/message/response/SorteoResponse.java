package com.devteam.fantasy.message.response;

public class SorteoResponse {

    private Long id;

    private String nombre;
    
    private String hour;
    
    private String day;

    private Double total;

    private Double comision;

    private Double riesgo;

    private String estado;

    private String moneda;

    private String type;

    public SorteoResponse(Long id, String nombre, Double total, Double comision, Double riesgo) {
        this.id = id;
        this.nombre = nombre;
        this.total = total;
        this.comision = comision;
        this.riesgo = riesgo;
    }

    public SorteoResponse(Long id, String nombre, Double total, Double comision, Double riesgo, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.total = total;
        this.comision = comision;
        this.riesgo = riesgo;
        this.estado = estado;
    }

    public SorteoResponse() {
    }

    public SorteoResponse(Long id, String nombre, Double total, Double comision, Double riesgo, String estado, String moneda) {
        this.id = id;
        this.nombre = nombre;
        this.total = total;
        this.comision = comision;
        this.riesgo = riesgo;
        this.estado = estado;
        this.moneda = moneda;
    }

    public SorteoResponse(Long id, String nombre, String day, String hour, Double total, Double comision, Double riesgo, String estado, String moneda, String type) {
        this.id = id;
        this.nombre = nombre;
        this.total = total;
        this.comision = comision;
        this.riesgo = riesgo;
        this.estado = estado;
        this.moneda = moneda;
        this.type = type;
        this.day = day;
        this.hour = hour;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getComision() {
        return comision;
    }

    public void setComision(Double comision) {
        this.comision = comision;
    }

    public Double getRiesgo() {
        return riesgo;
    }

    public void setRiesgo(Double riesgo) {
        this.riesgo = riesgo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}
    
}
