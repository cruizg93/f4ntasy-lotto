package com.devteam.fantasy.service;

import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;

import com.devteam.fantasy.exception.CanNotInsertApuestaException;
import com.devteam.fantasy.exception.CanNotInsertHistoricoBalanceException;
import com.devteam.fantasy.exception.CanNotRemoveApuestaException;
import com.devteam.fantasy.exception.InvalidSorteoStateException;
import com.devteam.fantasy.exception.SorteoEstadoNotValidException;
import com.devteam.fantasy.message.response.ApuestaActivaResponse;
import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorSorteosResponse;
import com.devteam.fantasy.message.response.NumeroPlayerEntryResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.message.response.SorteosPasadosWeek;
import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;

@Transactional
public interface SorteoService {
	
	public List<SorteoDiaria> getActiveSorteosList(User user);
	public List<SorteoDiaria> getActiveSorteosList();
	public JugadorSorteosResponse getJugadorList();
	public List<SorteoResponse> getSorteosResponses(List<SorteoDiaria> sorteos, User user);
	
	public void setNumeroGanador(Long id, int numeroGanador) throws Exception;
	public List<ApuestasActivasResponse> getSorteosListWithMoneda(String currency);
	public ApuestaActivaResumenResponse getActiveSorteoDetail(Long id, String currency); 
	public Sorteo bloquearApuesta(Long id) throws InvalidSorteoStateException;
	public Sorteo desBloquearApuesta(Long id) throws InvalidSorteoStateException;
	
	public Sorteo forceCloseStatus(Long id);
	public ApuestaActivaResumenResponse getDetalleApuestasBySorteo(Long id, String monedatype);
	public void submitApuestas(String username, Long sorteoId, List<NumeroPlayerEntryResponse> apuestasEntry) throws CanNotInsertApuestaException, SorteoEstadoNotValidException;
	public ApuestaActivaResponse getApuestasActivasBySorteoAndJugador(Long sorteoId, String username);
	
	public void deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long sorteoId, Integer numero, String username) throws CanNotRemoveApuestaException, SorteoEstadoNotValidException;
	public void deleteAllApuestasOnSorteoDiarioByUser(Long sorteoId, String username) throws CanNotRemoveApuestaException, SorteoEstadoNotValidException;
	public void cerrarSemana(SorteoDiaria sorteoDiaria) throws CanNotInsertHistoricoBalanceException;
	
}
