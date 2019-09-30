package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.math.SorteoTotales;
import com.devteam.fantasy.message.response.PairDayBalance;
import com.devteam.fantasy.message.response.SorteoNumeroGanador;
import com.devteam.fantasy.message.response.SorteosPasadosApuestas;
import com.devteam.fantasy.message.response.SorteosPasadosJugador;
import com.devteam.fantasy.message.response.SorteosPasadosWeek;
import com.devteam.fantasy.message.response.SummaryResponse;
import com.devteam.fantasy.message.response.WeekResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Bono;
import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.HistoryEvent;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.NumeroGanador;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.repository.BonoRepository;
import com.devteam.fantasy.repository.HistoricoApuestaRepository;
import com.devteam.fantasy.repository.HistoricoBalanceRepository;
import com.devteam.fantasy.repository.HistoryEventRepository;
import com.devteam.fantasy.repository.NumeroGanadorRepository;
import com.devteam.fantasy.repository.SorteoRepository;
import com.devteam.fantasy.repository.UserRepository;
import com.devteam.fantasy.repository.WeekRepository;
import com.devteam.fantasy.util.BalanceType;
import com.devteam.fantasy.util.HistoryEventType;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.PairNV;
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
	
	@Autowired 
	NumeroGanadorRepository numeroGanadorRepository;
	
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
//			Set<SorteosPasadosJugador> jugadores 		= getSorteosPasadosJugadorByWeek(historicoApuestas, week);
//			sorteosPasadosWeek.setJugadores(jugadores);
			
			logger.debug("sorteoTotales.processHitoricoApuestas");
//			sorteoTotales.processHitoricoApuestas(historicoApuestas, sorteosPasadosWeek, monedaName);
			
		}catch (Exception e) {
			logger.error("getSorteosPasadosByWeek(Long {},String {}): CATCH", weekID,moneda);
			logger.error(e.getMessage());
			e.printStackTrace();
		}finally {
			logger.debug("getSorteosPasadosByWeek(Long weekID,String moneda): END");
		}
		return sorteosPasadosWeek;
	}
	
	private List<HistoricoApuestas> getHistoricoApuestasByWeek(List<Sorteo> sorteos) {
		List<HistoricoApuestas> historicoApuestas = new ArrayList<>();  
		sorteos.forEach(sorteo ->{
//			historicoApuestas.addAll(historicoApuestaRepository.findAllBySorteo(sorteo));
		});
		return historicoApuestas;
	}
	
	@Override
	public SorteosPasadosJugador getSorteosPasadosJugadorByWeek(Long weekId, Long jugadorId) throws Exception {
		User user 									= userService.getById(jugadorId);
		Jugador jugador 							= Util.getJugadorFromUser(user);
		return getSorteosPasadosJugadorByWeek(weekId, jugador);
	}
	
	@Override
	public SorteosPasadosJugador getSorteosPasadosJugadorByWeek(Long weekId, Jugador jugador) throws Exception {
		try {
			logger.debug("getSorteosPasadosJugadorByWeek(Long {},jugador {}): START", weekId,jugador.getId());
		
			Week week 									= weekRepository.findById(weekId).orElseThrow(() -> new NotFoundException("Not Week Found"));
			SorteosPasadosJugador sorteosPasadosJugador = new SorteosPasadosJugador();
			List<Sorteo> sorteos 						= sorteoRepository.findAllBySorteoTimeBetween(week.getMonday(),week.getSunday());
			List<PairDayBalance> pairDaysBalance 		= new ArrayList<>();
			
			BigDecimal comisionWeek 	= BigDecimal.ZERO;
			BigDecimal premiosWeek 		= BigDecimal.ZERO;
			BigDecimal ventasWeek 		= BigDecimal.ZERO;
			BigDecimal subTotalWeek	 	= BigDecimal.ZERO;
			BigDecimal comisionDay 		= BigDecimal.ZERO;
			BigDecimal premiosDay 		= BigDecimal.ZERO;
			BigDecimal ventasDay 		= BigDecimal.ZERO;
			BigDecimal subTotalDay		= BigDecimal.ZERO;
			double prevBalance 		= 0d; 

			SorteoNumeroGanador sorteo11 = new SorteoNumeroGanador();
			SorteoNumeroGanador sorteo12 = new SorteoNumeroGanador();
			SorteoNumeroGanador sorteo15 = new SorteoNumeroGanador();
			SorteoNumeroGanador sorteo21 = new SorteoNumeroGanador();
			
			for(Sorteo sorteo: sorteos) {
				
				HistoricoBalance historicoBalance = historicoBalanceRepository.findBySorteoTimeAndJugador(sorteo.getSorteoTime(), jugador);
				prevBalance = historicoBalance!=null?historicoBalance.getBalanceSemana():prevBalance;
				
				List<HistoricoApuestas> apuestas = historicoApuestaRepository.findAllBySorteoAndUser(sorteo, jugador);
				SummaryResponse summarySorteo = sorteoTotales.processHitoricoApuestas(apuestas);

				comisionWeek	= comisionWeek.add( new BigDecimal(summarySorteo.getComisiones()));
				premiosWeek		= premiosWeek.add(new BigDecimal(summarySorteo.getPremios()));
				ventasWeek	 	= ventasWeek.add(new BigDecimal(summarySorteo.getVentas()));
				subTotalWeek	= subTotalWeek.add(new BigDecimal(summarySorteo.getSubTotal()));
				comisionDay	= comisionDay.add( new BigDecimal(summarySorteo.getComisiones()));
				premiosDay	= premiosDay.add(new BigDecimal(summarySorteo.getPremios()));
				ventasDay	= ventasDay.add(new BigDecimal(summarySorteo.getVentas()));
				subTotalDay	= subTotalDay.add(new BigDecimal(summarySorteo.getSubTotal()));
				
				LocalDateTime sorteoTime = sorteo.getSorteoTime().toLocalDateTime();
				NumeroGanador numeroGanador = numeroGanadorRepository.getBySorteo(sorteo);
				
				if(sorteoTime.getHour() == 11) {
					sorteo11 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"11 am");
				}else if(sorteoTime.getHour() == 12) {
					sorteo12 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"12 pm");
				} else if(sorteoTime.getHour() == 15) {
					sorteo15 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"3 pm");
				}else if(sorteoTime.getHour() == 21) {
					sorteo21 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"9 pm");
					
					SummaryResponse summaryDay = new SummaryResponse();
					summaryDay.setComisiones(comisionDay.doubleValue());
					summaryDay.setCurrency(summarySorteo.getCurrency());
					summaryDay.setPremios(premiosDay.doubleValue());
					summaryDay.setVentas(ventasDay.doubleValue());
					summaryDay.setSubTotal(subTotalDay.doubleValue());
					
					PairDayBalance sorteosPasado = new PairDayBalance();
					sorteosPasado.setSorteoTime(Util.getDayFromTimestamp(sorteo.getSorteoTime()));
					sorteosPasado.setBalance(historicoBalance!=null?historicoBalance.getBalanceSemana():prevBalance);
					sorteosPasado.setSummary(summaryDay);
					pairDaysBalance.add(sorteosPasado);
					
					List<SorteoNumeroGanador> numerosGanadores = new ArrayList<>();
					numerosGanadores.add(sorteo11);
					if(sorteoTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
						numerosGanadores.add(sorteo12);
					}
					numerosGanadores.add(sorteo15);
					numerosGanadores.add(sorteo21);
					sorteosPasado.setSorteos(numerosGanadores);
					
					prevBalance = 0d;
					sorteo11 	= new SorteoNumeroGanador();
					sorteo12 	= new SorteoNumeroGanador();
					sorteo15 	= new SorteoNumeroGanador();
					sorteo21 	= new SorteoNumeroGanador();
					comisionDay = BigDecimal.ZERO;
					premiosDay 	= BigDecimal.ZERO;
					ventasDay 	= BigDecimal.ZERO;
					subTotalDay	= BigDecimal.ZERO;
				}
				
			}
			
			SummaryResponse summary = new SummaryResponse();
			Bono bono = bonoRepository.findByWeekAndUser(week, jugador);
			summary.setBonos(bono != null ?bono.getBono():0d);
			summary.setComisiones(comisionWeek.doubleValue());
			summary.setPremios(premiosWeek.doubleValue());
			summary.setVentas(ventasWeek.doubleValue());
			summary.setSubTotal(subTotalWeek.doubleValue());
			summary.setCurrency(jugador.getMoneda().getMonedaName().toString());

			sorteosPasadosJugador.setSorteosPasados(pairDaysBalance);
			sorteosPasadosJugador.setSummary(summary);
			
			return sorteosPasadosJugador;
		}catch (Exception e) {
			logger.debug("getSorteosPasadosJugadorByWeek(Long {},jugador {}): CATCH", weekId,jugador.getId());
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		finally {
			logger.debug("getSorteosPasadosJugadorByWeek(Long weekId,jugador jugadorId): END");
		}
	}

	@Override
	public List<WeekResponse> getAllWeeks() {
		List<Week> weeks = weekRepository.findAllByOrderByIdDesc();
		List<WeekResponse> response = new ArrayList<WeekResponse>();
		weeks.forEach(week -> {
			WeekResponse w = new WeekResponse();
			w.setId(week.getId());
			w.setYear(week.getYear());
			w.setMonday(Util.getShortDayFromTimestamp(week.getMonday()));
			w.setSunday(Util.getShortDayFromTimestamp(week.getSunday()));
			response.add(w);
		});
		return response;
	}
	
	public boolean isJugadorElegibleForBono(Jugador jugador, Week week) {
		HistoricoBalance weekBalance = historicoBalanceRepository
				.findByBalanceTypeAndJugadorAndWeek(BalanceType.WEEKLY,jugador,week)
				.orElse(new HistoricoBalance());
		return weekBalance.getBalanceSemana()<0?true:false;
	}
	
	private SorteoNumeroGanador buildSorteoNumeroGanador(Sorteo sorteo, Integer numeroGanador, String hour) {
		SorteoNumeroGanador sorteoNumeroGanador = new SorteoNumeroGanador();
		sorteoNumeroGanador.setHour(hour);
		sorteoNumeroGanador.setId(String.valueOf(sorteo.getId()));
		sorteoNumeroGanador.setNumero(String.valueOf(numeroGanador));
		sorteoNumeroGanador.setType(sorteo.getSorteoType().getSorteoTypeName().toString());
		return sorteoNumeroGanador;
	}

	@Override
	public SorteosPasadosApuestas getApuestasPasadasBySorteoAndJugador(Long sorteoId, Jugador jugador) throws Exception {
		SorteosPasadosApuestas sorteosPasadosApuestas = new SorteosPasadosApuestas();
		Sorteo sorteo;
		try {
			logger.debug("getApuestasPasadasBySorteoAndJugador(Long {}, Jugador {}): START", sorteoId, jugador.getId());
			sorteo = sorteoRepository.findById(sorteoId).orElseThrow(() -> new NotFoundException("Sorteo not found with id: "+sorteoId));
			List<HistoricoApuestas> apuestas = historicoApuestaRepository.findAllBySorteoAndUser(sorteo, jugador);
			List<PairNV> pairs = mergeApuestasIntoPairNVList(apuestas);
			boolean hasApuestasMadeByAsistente = false;
			
			BigDecimal premios = BigDecimal.ZERO;
			
			for(HistoricoApuestas apuesta: apuestas) {
				BigDecimal premio = BigDecimal.valueOf(apuesta.getPremioMultiplier()).multiply(BigDecimal.valueOf(apuesta.getCantidad()));
				premios = premios.add(premio);
				
				if(apuesta.getAsistente()!=null && !hasApuestasMadeByAsistente) {
					hasApuestasMadeByAsistente = true;
				}
			}
			SummaryResponse summary = sorteoTotales.processHitoricoApuestas(apuestas);
			sorteosPasadosApuestas.setxApuestas(hasApuestasMadeByAsistente);
			sorteosPasadosApuestas.setApuestas(pairs);
			sorteosPasadosApuestas.setSummary(summary);
		} catch (Exception e) {
			logger.error("getApuestasPasadasBySorteoAndJugador(Long {}, Jugador {}): CATCH", sorteoId, jugador.getId());
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}finally {
			logger.debug("getApuestasPasadasBySorteoAndJugador(Long sorteoId, Jugador jugador): END");
		}
		return sorteosPasadosApuestas;		
	}
	
	private List<PairNV> mergeApuestasIntoPairNVList(List<HistoricoApuestas> apuestas) {
		List<PairNV> pairNVList = new ArrayList<>();
		for (HistoricoApuestas apuesta : apuestas) {
			PairNV jugadorPair = pairNVList.stream()
					.filter(i -> i.getNumero() == apuesta.getNumero()).findFirst().orElse(null);
			if (jugadorPair == null) {
				jugadorPair = new PairNV(apuesta.getNumero(), apuesta.getCantidad());
				jugadorPair.setNumeroText(apuesta.getNumero()<10?"0"+apuesta.getNumero():String.valueOf(apuesta.getNumero()));
				pairNVList.add(jugadorPair);
			} else {
				jugadorPair.setValor(jugadorPair.getValor() + apuesta.getCantidad());
			}
		}
		pairNVList.sort((pair1, pair2) -> pair1.getNumero().compareTo(pair2.getNumero()));
		return pairNVList;
	}

}
