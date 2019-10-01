package com.devteam.fantasy.service;

import java.util.List;
import java.util.Set;

import com.devteam.fantasy.message.response.NumeroGanadorSorteoResponse;
import com.devteam.fantasy.message.response.SorteosPasadosApuestas;
import com.devteam.fantasy.message.response.SorteosPasadosJugador;
import com.devteam.fantasy.message.response.SorteosPasadosWeek;
import com.devteam.fantasy.message.response.WeekResponse;
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
	SorteosPasadosWeek getSorteosPasadosByWeek(Long weekID, String moneda) throws Exception;
	SorteosPasadosJugador getSorteosPasadosJugadorByWeek(Long weekId, Long jugadorId) throws Exception;
	SorteosPasadosJugador getSorteosPasadosJugadorByWeek(Long weekId, Jugador jugador) throws Exception;
	
	List<WeekResponse> getAllWeeks();
	boolean isJugadorElegibleForBono(Jugador jugador, Week week);
	SorteosPasadosApuestas getApuestasPasadasBySorteoAndJugador(Long sorteoId, Jugador jugador) throws Exception;

	List<NumeroGanadorSorteoResponse> getNumerosGanadores(String currency) throws Exception;
	
}
