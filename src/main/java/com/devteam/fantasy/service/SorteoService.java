package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;

public interface SorteoService {
	
	public List<SorteoDiaria> getPlayerSorteosList(User user);
	
	public void setNumeroGanador(Long id, int numeroGanador);
}
