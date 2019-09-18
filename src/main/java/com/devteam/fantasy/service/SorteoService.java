package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.exception.ApuestaNotFoundException;
import com.devteam.fantasy.exception.CanNotInsertApuestaException;
import com.devteam.fantasy.exception.InvalidSorteoStateException;
import com.devteam.fantasy.message.response.ApuestaActivaResponse;
import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorSorteosResponse;
import com.devteam.fantasy.message.response.NumeroPlayerEntryResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.util.PairNV;

public interface SorteoService {
	
	public List<SorteoDiaria> getActiveSorteosList(User user);
	public List<SorteoDiaria> getActiveSorteosList();
	public JugadorSorteosResponse getJugadorList();
	public List<SorteoResponse> getSorteosResponses(List<SorteoDiaria> sorteos, User user);
	
	public void setNumeroGanador(Long id, int numeroGanador);
	public List<ApuestasActivasResponse> getSorteosListWithMoneda(String currency);
	public ApuestaActivaResumenResponse getActiveSorteoDetail(Long id, String currency); 
	public Sorteo bloquearApuesta(Long id) throws InvalidSorteoStateException;
	public Sorteo desBloquearApuesta(Long id) throws InvalidSorteoStateException;
	
	public Sorteo forceCloseStatus(Long id);
	public ApuestaActivaResumenResponse getDetalleApuestasBySorteo(Long id, String monedatype);
	public void submitApuestas(String username, Long sorteoId, List<NumeroPlayerEntryResponse> apuestasEntry) throws CanNotInsertApuestaException;
	public ApuestaActivaResponse getApuestasActivasBySorteoAndJugador(Long sorteoId, String username);
	
	public void deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long sorteoId, Integer numero, String username);
	public void deleteAllApuestasOnSorteoDiarioByUser(Long sorteoId, String username);
}
