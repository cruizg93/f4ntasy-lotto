package com.devteam.fantasy.message.response;

import java.util.List;

public class SorteosPasadosJugador {

	private SummaryResponse summary;
	
	private List<PairDayBalance> sorteosPasados;

	public SorteosPasadosJugador() {
		super();
	}
	
	public SummaryResponse getSummary() {
		return summary;
	}

	public void setSummary(SummaryResponse summary) {
		this.summary = summary;
	}

	public List<PairDayBalance> getSorteosPasados() {
		return sorteosPasados;
	}

	public void setSorteosPasados(List<PairDayBalance> sorteosPasados) {
		this.sorteosPasados = sorteosPasados;
	}
	
	
}
