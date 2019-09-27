package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.message.response.SorteosPasadosWeek;
import com.devteam.fantasy.model.HistoryEvent;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.util.HistoryEventType;

public interface HistoryService {

	HistoryEvent createEvent(HistoryEventType eventType);
	HistoryEvent createEvent(HistoryEventType eventType, Long keyValue);
	HistoryEvent createEvent(HistoryEventType eventType, Long keyValue, String oldValue, String newValue); 
	
	List<HistoryEvent> getAllByUser(User user);
	SorteosPasadosWeek getSorteosPasadosByWeek(Long weekID, String moneda) throws Exception;

	List<Week> getAllWeeks();
	boolean isJugadorElegibleForBono(Jugador jugador, Week week);
}
