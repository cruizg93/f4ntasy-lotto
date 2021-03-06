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
import com.devteam.fantasy.message.response.ApuestaActivaDetallesResponse;
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
	
	public void setNumeroGanador(Long id, int numeroGanador) throws CanNotInsertWinningNumberException, CanNotInsertHistoricoBalanceException, NotFoundException;
	public List<ApuestasActivasResponse> getSorteosListWithMoneda(String currency) throws Exception;
	public ApuestaActivaResumenResponse getActiveSorteoDetail(Long id, String currency) throws Exception; 
	public Sorteo bloquearApuesta(Long id) throws InvalidSorteoStateException;
	public Sorteo desBloquearApuesta(Long id) throws InvalidSorteoStateException;
	
	public Sorteo forceOpenStatus(Long id) throws Exception;
	public Sorteo forceCloseStatus(Long id);
	public ApuestaActivaResumenResponse getDetalleApuestasBySorteo(Long id, String monedatype) throws Exception;
	public void submitApuestas(String username, Long sorteoId, List<NumeroPlayerEntryResponse> apuestasEntry) throws CanNotInsertApuestaException, SorteoEstadoNotValidException, NotFoundException;
	public ApuestaActivaResponse getApuestasActivasBySorteoAndJugador(Long sorteoId, String username) throws Exception;
	public List<ApuestaActivaDetallesResponse> getApuestasActivasDetallesBySorteoAndJugador(Long sorteoId, String username) throws NotFoundException;
	
	
	public void deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long sorteoId, Integer numero, User user) throws CanNotRemoveApuestaException, SorteoEstadoNotValidException;
	public void deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long sorteoId, Integer numero) throws CanNotRemoveApuestaException, SorteoEstadoNotValidException;
	public void deleteAllApuestasOnSorteoDiarioByUser(Long sorteoId, User user) throws CanNotRemoveApuestaException, SorteoEstadoNotValidException, NotFoundException;
	public void deleteAllApuestasOnSorteoDiarioByUser(Long sorteoId) throws CanNotRemoveApuestaException, SorteoEstadoNotValidException, NotFoundException;
	
	
	public void cerrarSemana(SorteoDiaria sorteoDiaria, Week week) throws CanNotInsertHistoricoBalanceException;
	public void changeWinningNumber(int newWinningNumber, Long sorteoId) throws NotFoundException, CanNotChangeWinningNumberException;
	public void copyApuestasToHistoricoApuestas(SorteoDiaria sorteoDiaria);
	public void deleteAllApuestasDetallesXOnSorteoDiarioByNumeroAndUser(Long sorteoId, Integer numero, User user)
			throws CanNotRemoveApuestaException, SorteoEstadoNotValidException;
	
	public void resetDayForUAT() throws Exception;
	
}
