package com.devteam.fantasy.service;

import java.util.List;

import javax.transaction.Transactional;

import com.devteam.fantasy.exception.CanNotChangeWinningNumberException;
import com.devteam.fantasy.exception.CanNotInsertApuestaException;
import com.devteam.fantasy.exception.CanNotInsertHistoricoBalanceException;
import com.devteam.fantasy.exception.CanNotInsertWinningNumberException;
import com.devteam.fantasy.exception.CanNotRemoveApuestaException;
import com.devteam.fantasy.exception.InvalidSorteoStateException;
import com.devteam.fantasy.exception.SorteoEstadoNotValidException;
import com.devteam.fantasy.message.response.ApuestaActivaResponse;
import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorSorteosResponse;
import com.devteam.fantasy.message.response.NumeroPlayerEntryResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;

import javassist.NotFoundException;

@Transactional
public interface SorteoService {
	
	public List<SorteoDiaria> getActiveSorteosList(User user) throws Exception;
	public List<SorteoDiaria> getActiveSorteosList() throws Exception;
	public JugadorSorteosResponse getJugadorList() throws Exception;
	public List<SorteoResponse> getSorteosResponses(List<SorteoDiaria> sorteos, User user);
	
	public void setNumeroGanador(Long id, int numeroGanador) throws CanNotInsertWinningNumberException, CanNotInsertHistoricoBalanceException;
	public List<ApuestasActivasResponse> getSorteosListWithMoneda(String currency) throws Exception;
	public ApuestaActivaResumenResponse getActiveSorteoDetail(Long id, String currency); 
	public Sorteo bloquearApuesta(Long id) throws InvalidSorteoStateException;
	public Sorteo desBloquearApuesta(Long id) throws InvalidSorteoStateException;
	
	public Sorteo forceCloseStatus(Long id);
	public ApuestaActivaResumenResponse getDetalleApuestasBySorteo(Long id, String monedatype);
	public void submitApuestas(String username, Long sorteoId, List<NumeroPlayerEntryResponse> apuestasEntry) throws CanNotInsertApuestaException, SorteoEstadoNotValidException;
	public ApuestaActivaResponse getApuestasActivasBySorteoAndJugador(Long sorteoId, String username);
	
	public void deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long sorteoId, Integer numero, String username) throws CanNotRemoveApuestaException, SorteoEstadoNotValidException;
	public void deleteAllApuestasOnSorteoDiarioByUser(Long sorteoId, String username) throws CanNotRemoveApuestaException, SorteoEstadoNotValidException;
	public void cerrarSemana(SorteoDiaria sorteoDiaria, Week week) throws CanNotInsertHistoricoBalanceException;
	public void changeWinningNumber(int newWinningNumber, Long sorteoId) throws NotFoundException, CanNotChangeWinningNumberException;
	public void copyApuestasToHistoricoApuestas(SorteoDiaria sorteoDiaria);
	
}
