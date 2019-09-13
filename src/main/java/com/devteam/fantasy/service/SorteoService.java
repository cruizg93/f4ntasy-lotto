package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;

public interface SorteoService {
	
	public List<SorteoDiaria> getActiveSorteosList(User user);
	public List<SorteoDiaria> getActiveSorteosList();
	
	public void setNumeroGanador(Long id, int numeroGanador);
	public ApuestaActivaResumenResponse getActiveSorteoDetail(Long id, String currency); 
}
