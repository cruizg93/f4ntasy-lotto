package com.devteam.fantasy.message.response;

import java.util.List;

import com.devteam.fantasy.util.PairNV;

public class SorteosPasadosApuestas {

	private SummaryResponse summary;
	
	private List<PairNV> apuestas;
	
	private boolean xApuestas;

	public SummaryResponse getSummary() {
		return summary;
	}

	public void setSummary(SummaryResponse summary) {
		this.summary = summary;
	}

	public List<PairNV> getApuestas() {
		return apuestas;
	}

	public void setApuestas(List<PairNV> apuestas) {
		this.apuestas = apuestas;
	}

	public boolean isxApuestas() {
		return xApuestas;
	}

	public void setxApuestas(boolean xApuestas) {
		this.xApuestas = xApuestas;
	}
}
