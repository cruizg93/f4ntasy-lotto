package com.devteam.fantasy.message.response;

import java.util.List;

public class PairDayBalance {

	private String sorteoTime;
	
	private double balance;
	
	private List<SorteoNumeroGanador> sorteos;
	
	private SummaryResponse summary;

	public String getSorteoTime() {
		return sorteoTime;
	}

	public void setSorteoTime(String sorteoTime) {
		this.sorteoTime = sorteoTime;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public List<SorteoNumeroGanador> getSorteos() {
		return sorteos;
	}

	public void setSorteos(List<SorteoNumeroGanador> sorteos) {
		this.sorteos = sorteos;
	}

	public SummaryResponse getSummary() {
		return summary;
	}

	public void setSummary(SummaryResponse summary) {
		this.summary = summary;
	}

	@Override
	public String toString() {
		return "PairDayBalance [sorteoTime=" + sorteoTime + ", balance=" + balance + ", sorteos=" + sorteos
				+ ", summary=" + summary + "]";
	}

}
