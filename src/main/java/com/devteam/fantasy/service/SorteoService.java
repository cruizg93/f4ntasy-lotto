package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorSorteosResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;

public interface SorteoService {
	
	public List<SorteoDiaria> getActiveSorteosList(User user);
	public List<SorteoDiaria> getActiveSorteosList();
	public JugadorSorteosResponse getJugadorList();
	public List<SorteoResponse> getSorteosResponses(List<SorteoDiaria> sorteos, User user);
	
	public void setNumeroGanador(Long id, int numeroGanador);
	public List<ApuestasActivasResponse> getSorteosListWithMoneda(String currency);
	public ApuestaActivaResumenResponse getActiveSorteoDetail(Long id, String currency); 
}
