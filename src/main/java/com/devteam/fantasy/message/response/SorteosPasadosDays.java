package com.devteam.fantasy.message.response;

import java.util.List;

public class SorteosPasadosDays extends SorteosPasados {

	
	private List<PairDayBalance> sorteosPasados;

	public SorteosPasadosDays() {
		super();
	}

	public List<PairDayBalance> getSorteosPasados() {
		return sorteosPasados;
	}

	public void setSorteosPasados(List<PairDayBalance> sorteosPasados) {
		this.sorteosPasados = sorteosPasados;
	}
	
	
}
