package com.devteam.fantasy.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.math.SorteoTotales;
import com.devteam.fantasy.message.response.SorteosPasadosJugador;
import com.devteam.fantasy.message.response.SorteosPasadosWeek;
import com.devteam.fantasy.model.Bono;
import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.HistoryEvent;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.repository.BonoRepository;
import com.devteam.fantasy.repository.HistoricoApuestaRepository;
import com.devteam.fantasy.repository.HistoricoBalanceRepository;
import com.devteam.fantasy.repository.HistoryEventRepository;
import com.devteam.fantasy.repository.SorteoRepository;
import com.devteam.fantasy.repository.WeekRepository;
import com.devteam.fantasy.util.BalanceType;
import com.devteam.fantasy.util.HistoryEventType;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.Util;

import javassist.NotFoundException;

@Service
public class HistoryServiceImpl implements HistoryService {

	@Autowired
	UserService userService;

	@Autowired
	HistoryEventRepository historyEventRepository;
	
	@Autowired
	SorteoTotales sorteoTotales;

	@Autowired
	SorteoRepository sorteoRepository;
	
	@Autowired
	HistoricoApuestaRepository historicoApuestaRepository;
	
	@Autowired
	HistoricoBalanceRepository historicoBalanceRepository;
	
	@Autowired
	BonoRepository bonoRepository;
	
	@Autowired 
	WeekRepository weekRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(HistoryServiceImpl.class);
	
	@Override
	public HistoryEvent createEvent(HistoryEventType eventType) {
		return createEvent(eventType, null ,null, null);
	}
	
	@Override
	public HistoryEvent createEvent(HistoryEventType eventType, Long keyValue) {
		return createEvent(eventType, keyValue, null, null);
	}
	
	@Override
	public HistoryEvent createEvent(HistoryEventType eventType, Long keyValue,String oldValue, String newValue) {
		User user = userService.getLoggedInUser();
		HistoryEvent event = new HistoryEvent(eventType, user);
		event.setOldValue(oldValue);
		event.setNewValue(newValue);
		event.setKeyValue(keyValue);
		return historyEventRepository.save(event);
	}

	@Override
	public List<HistoryEvent> getAllByUser(User user) {
		logger.debug("getAllByUser(User {}): START", user.getUsername());
		logger.debug("getAllByUser(User user): END");
		return historyEventRepository.findAllByUserOrderByCreatedDate(user);
	}

	@Override
	public SorteosPasadosWeek getSorteosPasadosByWeek(Long weekID, String moneda) throws Exception {
		SorteosPasadosWeek sorteosPasadosWeek = new SorteosPasadosWeek();
		try {
			logger.debug("getSorteosPasadosByWeek(Long {},String {}): START", weekID,moneda);
			Week week = weekRepository.findById(weekID).orElseThrow(() -> new NotFoundException("Week not found"));
			
			MonedaName monedaName 						= Util.getMonedaNameFromString(moneda);
			List<Sorteo> sorteos 						= sorteoRepository.getAllBetweenTimestamp(week.getMonday(), week.getSunday());
			List<HistoricoApuestas> historicoApuestas 	= getHistoricoApuestasByWeek(sorteos);
			
			logger.debug("getSorteosPasadosJugadorByWeek");
			Set<SorteosPasadosJugador> jugadores 		= getSorteosPasadosJugadorByWeek(historicoApuestas, week);
			sorteosPasadosWeek.setJugadores(jugadores);
			
			logger.debug("sorteoTotales.processHitoricoApuestas");
			sorteoTotales.processHitoricoApuestas(historicoApuestas, sorteos, sorteosPasadosWeek, monedaName);
			
		}catch (Exception e) {
			logger.error("getSorteosPasadosByWeek(Long {},String {}): CATCH", weekID,moneda);
			logger.error(e.getMessage());
			logger.error(e.getStackTrace().toString());
		}finally {
			logger.debug("getSorteosPasadosByWeek(Long weekID,String moneda): END");
		}
		return sorteosPasadosWeek;
	}
	
	private List<HistoricoApuestas> getHistoricoApuestasByWeek(List<Sorteo> sorteos) {
		List<HistoricoApuestas> historicoApuestas = new ArrayList<>();  
		sorteos.forEach(sorteo ->{
			historicoApuestas.addAll(historicoApuestaRepository.findAllBySorteo(sorteo));
		});
		return historicoApuestas;
	}
	
	private Set<SorteosPasadosJugador> getSorteosPasadosJugadorByWeek(List<HistoricoApuestas> historicoApuestas, Week week ){
		Set<SorteosPasadosJugador> jugadores = new HashSet<>();
		for(HistoricoApuestas historicoApuesta: historicoApuestas) {
			Jugador jugador = Util.getJugadorFromUser(historicoApuesta.getUser());
			SorteosPasadosJugador sorteosPasadosJugador = new SorteosPasadosJugador(jugador.getId().toString());
			if( !jugadores.contains(sorteosPasadosJugador)) {
				HistoricoBalance historicoBalance = historicoBalanceRepository.findBySorteoTimeAndJugador(historicoApuesta.getSorteo().getSorteoTime(), jugador);
				sorteosPasadosJugador.setBalance(String.valueOf(historicoBalance.getBalanceSemana()));
				
				Bono bono = bonoRepository.findByWeekAndUser(week, jugador);
				sorteosPasadosJugador.setBono(bono.getBono().toString());
				sorteosPasadosJugador.setMoneda(historicoApuesta.getMoneda());
				sorteosPasadosJugador.setName(jugador.getName());
				sorteosPasadosJugador.setUsername(jugador.getUsername());
				
				jugadores.add(sorteosPasadosJugador);
			}
		}
		return jugadores;
	}

	@Override
	public List<Week> getAllWeeks() {
		return weekRepository.findAll();
	}
	
	public boolean isJugadorElegibleForBono(Jugador jugador, Week week) {
		HistoricoBalance weekBalance = historicoBalanceRepository
				.findByBalanceTypeAndJugadorAndWeek(BalanceType.WEEKLY,jugador,week)
				.orElse(new HistoricoBalance());
		return weekBalance.getBalanceSemana()<0?true:false;
	}
}
