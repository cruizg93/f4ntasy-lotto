package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.exception.CanNotInsertBonoException;
import com.devteam.fantasy.message.response.HistoricoApuestaDetallesResponse;
import com.devteam.fantasy.message.response.NumeroGanadorSorteoResponse;
import com.devteam.fantasy.message.response.SorteosPasadosApuestas;
import com.devteam.fantasy.message.response.SorteosPasadosDays;
import com.devteam.fantasy.message.response.SorteosPasadosJugadores;
import com.devteam.fantasy.message.response.SorteosPasados;
import com.devteam.fantasy.message.response.WeekResponse;
import com.devteam.fantasy.model.Bono;
import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.HistoryEvent;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.util.HistoryEventType;

import javassist.NotFoundException;

public interface HistoryService {

	HistoryEvent createEvent(HistoryEventType eventType);
	HistoryEvent createEvent(HistoryEventType eventType, Long keyValue);
	HistoryEvent createEvent(HistoryEventType eventType, Long keyValue, String oldValue, String newValue); 
	
	List<HistoryEvent> getAllByUser(User user);
	SorteosPasados getSorteosPasadosJugadorByWeek(Long weekId, Long jugadorId) throws Exception;
	SorteosPasados getSorteosPasadosJugadorByWeek(Long weekId, User user) throws Exception;
	
	List<WeekResponse> getAllWeeks();
	void validateIfJugadorIsElegibleForBono(Jugador jugador, Week week, Bono bono) throws CanNotInsertBonoException, NotFoundException;
	SorteosPasadosApuestas getApuestasPasadasBySorteoAndJugador(Long sorteoId, User jugador) throws Exception;

	List<NumeroGanadorSorteoResponse> getNumerosGanadores(String currency) throws Exception;
	List<HistoricoApuestaDetallesResponse> getHistoricoApuestaDetallesX(Long id) throws Exception;
	List<HistoricoApuestaDetallesResponse> getHistoricoApuestaDetallesX(Long id, User user) throws Exception;
	SorteosPasadosDays getSorteosPasadosCasaByWeek(Long weekID, String moneda) throws Exception;
	SorteosPasadosJugadores getSorteosPasadosJugadoresByWeek(Long weekID, String moneda) throws Exception;
	
	HistoricoBalance getWeekBalanceByJugador(Jugador jugador);
	
}
