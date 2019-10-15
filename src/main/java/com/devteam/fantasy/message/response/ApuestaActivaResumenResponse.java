package com.devteam.fantasy.message.response;

import com.devteam.fantasy.util.TuplaRiesgo;

import java.util.List;

public class ApuestaActivaResumenResponse {
    private TuplaRiesgo maxRiesgo;
    private List<TuplaRiesgo> tuplaRiesgos;
    private double comision;
    private double total;
    private double subTotal;
    private double totalDolar;
    private double totalLempira;

    public ApuestaActivaResumenResponse() {
    }

    public ApuestaActivaResumenResponse(TuplaRiesgo maxRiesgo, List<TuplaRiesgo> tuplaRiesgos,
                                        double comision) {
        this.maxRiesgo = maxRiesgo;
        this.tuplaRiesgos = tuplaRiesgos;
        this.comision = comision;
    }

    public ApuestaActivaResumenResponse(TuplaRiesgo maxRiesgo, List<TuplaRiesgo> tuplaRiesgos,
                                        double comision, double total, double totalLempira, double totalDolar) {
        this.maxRiesgo = maxRiesgo;
        this.tuplaRiesgos = tuplaRiesgos;
        this.comision = comision;
        this.total = total;
        this.totalLempira = totalLempira;
        this.totalDolar = totalDolar;
    }

    public TuplaRiesgo getMaxRiesgo() {
        return maxRiesgo;
    }

    public void setMaxRiesgo(TuplaRiesgo maxRiesgo) {
        this.maxRiesgo = maxRiesgo;
    }

    public List<TuplaRiesgo> getTuplaRiesgos() {
        return tuplaRiesgos;
    }

    public void setTuplaRiesgos(List<TuplaRiesgo> tuplaRiesgos) {
        this.tuplaRiesgos = tuplaRiesgos;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

	public double getSubTotal() {
		return this.total-this.comision;
	}

	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	public double getTotalDolar() {
		return totalDolar;
	}

	public void setTotalDolar(double totalDolar) {
		this.totalDolar = totalDolar;
	}

	public double getTotalLempira() {
		return totalLempira;
	}

	public void setTotalLempira(double totalLempira) {
		this.totalLempira = totalLempira;
	}
}
