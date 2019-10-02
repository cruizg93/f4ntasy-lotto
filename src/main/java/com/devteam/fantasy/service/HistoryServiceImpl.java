package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.math.MathUtil;
import com.devteam.fantasy.math.SorteoTotales;
import com.devteam.fantasy.message.response.HistoricoApuestaDetallesResponse;
import com.devteam.fantasy.message.response.JugadorBalanceWeek;
import com.devteam.fantasy.message.response.NumeroGanadorSorteoResponse;
import com.devteam.fantasy.message.response.PairDayBalance;
import com.devteam.fantasy.message.response.PairJP;
import com.devteam.fantasy.message.response.SorteoNumeroGanador;
import com.devteam.fantasy.message.response.SorteosPasadosApuestas;
import com.devteam.fantasy.message.response.SorteosPasadosDays;
import com.devteam.fantasy.message.response.SorteosPasados;
import com.devteam.fantasy.message.response.SorteosPasadosJugadores;
import com.devteam.fantasy.message.response.SummaryResponse;
import com.devteam.fantasy.message.response.WeekResponse;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Bono;
import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.HistoryEvent;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.NumeroGanador;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.repository.AsistenteRepository;
import com.devteam.fantasy.repository.BonoRepository;
import com.devteam.fantasy.repository.HistoricoApuestaRepository;
import com.devteam.fantasy.repository.HistoricoBalanceRepository;
import com.devteam.fantasy.repository.HistoryEventRepository;
import com.devteam.fantasy.repository.JugadorRepository;
import com.devteam.fantasy.repository.NumeroGanadorRepository;
import com.devteam.fantasy.repository.SorteoRepository;
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
	JugadorRepository jugadorRepository;
	
	@Autowired 
	NumeroGanadorRepository numeroGanadorRepository;

	@Autowired
	AsistenteRepository asistenteRepository;
	
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
	@PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
	public SorteosPasadosDays getSorteosPasadosCasaByWeek(Long weekId, String moneda) throws Exception {
		SorteosPasadosDays sorteosPasadosJugador	= new SorteosPasadosDays();
		try {
			logger.debug("getSorteosPasadosCasaByWeek(Long {},String {}): START", weekId,moneda);
			Week week 									= weekRepository.findById(weekId).orElseThrow(() -> new NotFoundException("Not Week Found"));
			
			List<Sorteo> sorteos 						= sorteoRepository.findAllBySorteoTimeBetweenOrderBySorteoTime(week.getMonday(),week.getSunday());
			List<PairDayBalance> pairDaysBalance 		= new ArrayList<>();
			
			BigDecimal comisionWeek 	= BigDecimal.ZERO;
			BigDecimal premiosWeek 		= BigDecimal.ZERO;
			BigDecimal ventasWeek 		= BigDecimal.ZERO;
			BigDecimal subTotalWeek	 	= BigDecimal.ZERO;
			BigDecimal comisionDay 		= BigDecimal.ZERO;
			BigDecimal premiosDay 		= BigDecimal.ZERO;
			BigDecimal ventasDay 		= BigDecimal.ZERO;
			BigDecimal subTotalDay		= BigDecimal.ZERO;
			BigDecimal prevBalance 		= BigDecimal.ZERO;

			SorteoNumeroGanador sorteo11 = new SorteoNumeroGanador();
			SorteoNumeroGanador sorteo12 = new SorteoNumeroGanador();
			SorteoNumeroGanador sorteo15 = new SorteoNumeroGanador();
			SorteoNumeroGanador sorteo21 = new SorteoNumeroGanador();
			
			for(Sorteo sorteo: sorteos) {
				
				List<HistoricoBalance> historicoBalanceList = historicoBalanceRepository.findAllBySorteoTime(sorteo.getSorteoTime());
				BigDecimal historicoBalance = BigDecimal.ZERO;
				for(HistoricoBalance hb: historicoBalanceList) {
					historicoBalance = historicoBalance.add(BigDecimal.valueOf(hb.getBalanceSemana()));
					
					double currencyExchange = MathUtil.getDollarChangeRateOriginalMoneda(hb.getCambio(),hb.getMoneda().getMonedaName().toString(), moneda);
					historicoBalance = historicoBalance.multiply(BigDecimal.valueOf(currencyExchange)); 
				}
				
				prevBalance = historicoBalance.compareTo(BigDecimal.ZERO)!=0?historicoBalance:prevBalance;
				
				List<HistoricoApuestas> apuestas = historicoApuestaRepository.findAllBySorteo(sorteo);
				SummaryResponse summarySorteo = sorteoTotales.processHitoricoApuestas(apuestas, moneda);

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
					
					BigDecimal incomeDay = premiosWeek;
					BigDecimal expensesDay = subTotalWeek;
					summaryDay.setPerdidasGanas(incomeDay.subtract(expensesDay).doubleValue());
					
					PairDayBalance sorteosPasado = new PairDayBalance();
					sorteosPasado.setSorteoTime(Util.getDayFromTimestamp(sorteo.getSorteoTime()));
					sorteosPasado.setBalance(historicoBalance.compareTo(BigDecimal.ZERO)==0?historicoBalance.doubleValue():prevBalance.doubleValue());
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
					
					prevBalance = BigDecimal.ZERO;
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
			List<Bono> bonoList = bonoRepository.findAllByWeek(week);
			BigDecimal bonos = BigDecimal.ZERO;
			for(Bono b: bonoList) {
				bonos = bonos.add(BigDecimal.valueOf(b.getBono()));
				
				double currencyExchange = MathUtil.getDollarChangeRateOriginalMoneda(b.getCambio(),b.getMoneda().getMonedaName().toString(), moneda);
				bonos = bonos.multiply(BigDecimal.valueOf(currencyExchange));
			}
			
			summary.setBonos(bonos.doubleValue());
			summary.setComisiones(comisionWeek.doubleValue());
			summary.setPremios(premiosWeek.doubleValue());
			summary.setVentas(ventasWeek.doubleValue());
			summary.setSubTotal(subTotalWeek.doubleValue());
			summary.setCurrency(moneda);
			
			BigDecimal income = premiosWeek.add(bonos);
			BigDecimal expenses = subTotalWeek;
			summary.setPerdidasGanas(income.subtract(expenses).doubleValue());

			sorteosPasadosJugador.setSorteosPasados(pairDaysBalance);
			sorteosPasadosJugador.setSummary(summary);
			
		}catch (Exception e) {
			logger.error("getSorteosPasadosCasaByWeek(Long {},String {}): CATCH", weekId,moneda);
			logger.error(e.getMessage());
			e.printStackTrace();
		}finally {
			logger.debug("getSorteosPasadosCasaByWeek(Long weekID,String moneda): END");
		}
		return sorteosPasadosJugador;
	}
	
	@Override
	@PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
	public SorteosPasadosJugadores getSorteosPasadosJugadoresByWeek(Long weekId, String moneda) throws Exception {
		SorteosPasadosJugadores result =  new SorteosPasadosJugadores();
		try {
			logger.debug("SorteosPasadosJugadores getSorteosPasadosJugadoresByWeek(Long {}, String {}): START", weekId,moneda);
			Week week 									= weekRepository.findById(weekId).orElseThrow(() -> new NotFoundException("Not Week Found"));
			List<Jugador> jugadores 					= jugadorRepository.findAllByOrderByIdAsc();
			Set<JugadorBalanceWeek> jugadoresResponse 	= new HashSet<JugadorBalanceWeek>();
			
			logger.debug("Creating Jugadores List...");
			for(Jugador jugador : jugadores) {
				JugadorBalanceWeek jugadorWeek = new JugadorBalanceWeek(); 
				
				Optional<HistoricoBalance> balance = historicoBalanceRepository.findByBalanceTypeAndJugadorAndWeek(BalanceType.WEEKLY, jugador, week);
				if( balance.isPresent()) {
					
					double currencyExchange = MathUtil.getDollarChangeRateOriginalMoneda(balance.get().getCambio(),balance.get().getMoneda().getMonedaName().toString(), moneda);
					BigDecimal balanceTotal = BigDecimal.valueOf(balance.get().getBalance()).multiply(BigDecimal.valueOf(currencyExchange));
					
					jugadorWeek.setBalance(balanceTotal.doubleValue());
				}
				
				Optional<Bono> bono = bonoRepository.findByWeekAndUser(week, jugador);
				if(bono.isPresent()) {
					jugadorWeek.setHaveBono(true);
				}
				
				jugadorWeek.setId(jugador.getId());
				jugadorWeek.setName(jugador.getName());
				jugadorWeek.setUsername(jugador.getUsername());
				jugadorWeek.setMoneda(moneda);
				
				jugadoresResponse.add(jugadorWeek);
			}
			
			logger.debug("Creating Week Summary...");
			List<HistoricoApuestas> apuestas = historicoApuestaRepository.findAllBySorteoSorteoTimeBetween(week.getMonday(),week.getSunday());
			SummaryResponse summarySorteo = sorteoTotales.processHitoricoApuestas(apuestas, moneda);
			
			result.setJugadores(jugadoresResponse);
			result.setSummary(summarySorteo);
			
		} catch (Exception e) {
			logger.error("SorteosPasadosJugadores getSorteosPasadosJugadoresByWeek(Long {}, String {}): CATCH", weekId,moneda);
			logger.error(e.getMessage());
			e.printStackTrace();
		}finally {
			logger.debug("SorteosPasadosJugadores getSorteosPasadosJugadoresByWeek(Long weekID, String moneda): END");
		}
		
		return result;
	}
	
	@Override
	@PreAuthorize("hasRole('USER') or hasRole('ASIS')")
	public SorteosPasadosDays getSorteosPasadosJugadorByWeek(Long weekId, Long jugadorId) throws Exception {
		User user 									= userService.getById(jugadorId);
		Jugador jugador 							= Util.getJugadorFromUser(user);
		return getSorteosPasadosJugadorByWeek(weekId, jugador);
	}
	
	@Override
	public SorteosPasadosDays getSorteosPasadosJugadorByWeek(Long weekId, Jugador jugador) throws Exception {
		try {
			logger.debug("getSorteosPasadosJugadorByWeek(Long {},jugador {}): START", weekId,jugador.getId());
		
			Week week 									= weekRepository.findById(weekId).orElseThrow(() -> new NotFoundException("Not Week Found"));
			SorteosPasadosDays sorteosPasadosJugador 	= new SorteosPasadosDays();
			List<Sorteo> sorteos 						= sorteoRepository.findAllBySorteoTimeBetweenOrderBySorteoTime(week.getMonday(),week.getSunday());
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
				SummaryResponse summarySorteo = sorteoTotales.processHitoricoApuestas(apuestas, jugador.getMoneda().getMonedaName().toString());

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
					
					BigDecimal income = premiosDay;
					BigDecimal expenses = subTotalDay;
					summaryDay.setPerdidasGanas(income.subtract(expenses).doubleValue());
					
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
			Optional<Bono> bonos = bonoRepository.findByWeekAndUser(week, jugador);
			summary.setBonos(bonos.isPresent()?bonos.get().getBono():0d);
			
			summary.setComisiones(comisionWeek.doubleValue());
			summary.setPremios(premiosWeek.doubleValue());
			summary.setVentas(ventasWeek.doubleValue());
			summary.setSubTotal(subTotalWeek.doubleValue());
			summary.setCurrency(jugador.getMoneda().getMonedaName().toString());
			
			BigDecimal income = premiosWeek.add(BigDecimal.valueOf(bonos.isPresent()?bonos.get().getBono():0d));
			BigDecimal expenses = subTotalWeek;
			summary.setPerdidasGanas(income.subtract(expenses).doubleValue());

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
			SummaryResponse summary = sorteoTotales.processHitoricoApuestas(apuestas, jugador.getMoneda().getMonedaName().toString());
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

	@Override
	public List<NumeroGanadorSorteoResponse> getNumerosGanadores(String currency) throws Exception {
		List<NumeroGanadorSorteoResponse> numerosGanadoresResponse = new ArrayList<NumeroGanadorSorteoResponse>();
		try {
			logger.debug("List<NumeroGanadorSorteoResponse> getNumerosGanadores(): START");
			List<NumeroGanador> numerosGanadores = numeroGanadorRepository.findAllByOrderBySorteoSorteoTimeDesc();
			
			for(NumeroGanador numero: numerosGanadores) {
				Map<Jugador,BigDecimal> jugadoresPremio 	= new HashMap<>();
				List<HistoricoApuestas> apuestas 		= historicoApuestaRepository.findAllBySorteoAndNumero(numero.getSorteo(), numero.getNumeroGanador());
				
				logger.debug("collecting and calculating premios...");
				for(HistoricoApuestas apuesta: apuestas){
					Double currencyExchange = MathUtil.getDollarChangeRateForHistorico(apuesta, MonedaName.DOLAR.toString().equalsIgnoreCase(currency)?MonedaName.DOLAR:MonedaName.LEMPIRA);
					Jugador jugador 		= Util.getJugadorFromUser(apuesta.getUser());
					BigDecimal premio 		= BigDecimal.valueOf(apuesta.getCantidad()).multiply(BigDecimal.valueOf(apuesta.getPremioMultiplier()));
					premio 					= premio.multiply(BigDecimal.valueOf(currencyExchange));
					
					if(jugadoresPremio.containsKey(jugador)) {
						premio = jugadoresPremio.get(jugador).add(premio);
					}
					
					jugadoresPremio.put(jugador, premio);
				}
				
				List<PairJP> premios = new ArrayList<PairJP>();
				BigDecimal premioSorteoTotal = BigDecimal.ZERO; 
				
				for (Map.Entry<Jugador, BigDecimal> jugadorPremio: jugadoresPremio.entrySet()) {
					PairJP pair = new PairJP();
					pair.setName(jugadorPremio.getKey().getName());
					pair.setUsername(jugadorPremio.getKey().getUsername());
					
					pair.setPremio(jugadorPremio.getValue().doubleValue());
					
					premios.add(pair);
					premioSorteoTotal = premioSorteoTotal.add(jugadorPremio.getValue());
				}
				
				
				logger.debug("creating NumeroGanadorSorteoResponse...");
				NumeroGanadorSorteoResponse numeroResponse = new NumeroGanadorSorteoResponse();
				numeroResponse.setNumero(numero.getNumeroGanador()<10?"0"+numero.getNumeroGanador():String.valueOf(numero.getNumeroGanador()));
				numeroResponse.setNumeroGanadorId(numero.getId());
				numeroResponse.setSorteoType(numero.getSorteo().getSorteoType().getSorteoTypeName().toString());
				
				numeroResponse.setJugadores(premios);
				numeroResponse.setPremio(premioSorteoTotal.doubleValue());
				numeroResponse.setDay(Util.getDayFromTimestamp(numero.getSorteo().getSorteoTime()));
				numeroResponse.setHour(Util.getHourFromTimestamp(numero.getSorteo().getSorteoTime()));
				
				numerosGanadoresResponse.add(numeroResponse);
			}
		} catch (Exception e) {
			logger.error("List<NumeroGanadorSorteoResponse> getNumerosGanadores(): CATCH");
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}finally {
			logger.debug("List<NumeroGanadorSorteoResponse> getNumerosGanadores(): END");
		}
		
		return numerosGanadoresResponse;
	}
	
	@Override
	public List<HistoricoApuestaDetallesResponse> getHistoricoApuestaDetallesX(Long id) {
		User user = userService.getLoggedInUser();
		return getHistoricoApuestaDetallesX(id, user);
	}
	
	@Override
	public List<HistoricoApuestaDetallesResponse> getHistoricoApuestaDetallesX(Long id, User user) {
		List<HistoricoApuestaDetallesResponse> apuestasDetails = new ArrayList<>();
        try {
        	logger.debug("List<HistoricoApuestaDetallesResponse> getHistoricoApuestaDetallesX(Long {}, User {}): START",id, user.getId());
        	Sorteo sorteo=sorteoRepository.getSorteoById(id);
            List<HistoricoApuestas> apuestas = historicoApuestaRepository.findAllBySorteoAndUserAndAsistenteOrderByNumeroAsc(sorteo, user,null);
            List<PairNV> pairNVList = new ArrayList<>();
            Jugador jugador= Util.getJugadorFromUser(user);
            int totalJugador = 0;
            
            for (HistoricoApuestas apuesta : apuestas) {
                pairNVList.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));
                totalJugador += apuesta.getCantidad();
            }
            
            HistoricoApuestaDetallesResponse detallesJugador = new HistoricoApuestaDetallesResponse();
            detallesJugador.setApuestas(pairNVList);
            detallesJugador.setUserId(jugador.getId());
            detallesJugador.setTitle("Apuestas de - " + user.getUsername());
            detallesJugador.setTotal(totalJugador);
            apuestasDetails.add(detallesJugador);
            
            List<Asistente> asistentes = asistenteRepository.findAllByJugador(jugador);
            asistentes.forEach(asistente -> {
                List<HistoricoApuestas> apuestaList = historicoApuestaRepository.findAllBySorteoAndUserAndAsistenteOrderByNumeroAsc(sorteo, user, asistente);
                if (apuestaList.size() > 0) {
                	HistoricoApuestaDetallesResponse detallesAsistente = new HistoricoApuestaDetallesResponse();
                    List<PairNV> pairNVListAsistente = new ArrayList<>();
                    int totalAsistente = 0;
                    for (HistoricoApuestas apuesta : apuestaList) {
                    	pairNVListAsistente.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));
                    	totalAsistente += apuesta.getCantidad();
                    }
                    
                    Collections.sort(pairNVListAsistente);
                    detallesAsistente.setApuestas(pairNVListAsistente);
                    detallesAsistente.setTitle("Apuestas de " + asistente.getUsername());
                    detallesAsistente.setUserId(asistente.getId());
                    detallesAsistente.setTotal(totalAsistente);
                    apuestasDetails.add(detallesAsistente);
                }
            });
		} catch (Exception e) {
			logger.error("List<HistoricoApuestaDetallesResponse> getHistoricoApuestaDetallesX(Long {}, User {}): CATCH",id, user.getId());
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}finally {
			logger.debug("List<HistoricoApuestaDetallesResponse> getHistoricoApuestaDetallesX(Long id, User user): END");
		}
        
        return apuestasDetails;
	}

}


















