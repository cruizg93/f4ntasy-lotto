package com.devteam.fantasy.message.response;

import com.devteam.fantasy.util.PairNV;

import java.util.List;

public class ApuestaActivaResponse {

	private boolean xApuestas;
	
    private String title;
    
    private String hour;
    
    private String day;

    private List<PairNV> list;

    private double total;

    private double comision;

    private double riesgo;

    private String type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PairNV> getList() {
        return list;
    }

    public void setList(List<PairNV> list) {
        this.list = list;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }

    public double getRiesgo() {
        return riesgo;
    }

    public void setRiesgo(double riesgo) {
        this.riesgo = riesgo;
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

	public boolean isxApuestas() {
		return xApuestas;
	}

	public void setxApuestas(boolean xApuestas) {
		this.xApuestas = xApuestas;
	}
}
