package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.exception.CanNotInsertBonoException;
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
import com.devteam.fantasy.message.response.SorteosPasadosJugadores;
import com.devteam.fantasy.message.response.SummaryResponse;
import com.devteam.fantasy.message.response.WeekResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Bono;
import com.devteam.fantasy.model.Estado;
import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.HistoryEvent;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.NumeroGanador;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoType;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.UserState;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.repository.AsistenteRepository;
import com.devteam.fantasy.repository.BonoRepository;
import com.devteam.fantasy.repository.EstadoRepository;
import com.devteam.fantasy.repository.HistoricoApuestaRepository;
import com.devteam.fantasy.repository.HistoricoBalanceRepository;
import com.devteam.fantasy.repository.HistoryEventRepository;
import com.devteam.fantasy.repository.JugadorRepository;
import com.devteam.fantasy.repository.NumeroGanadorRepository;
import com.devteam.fantasy.repository.SorteoRepository;
import com.devteam.fantasy.repository.WeekRepository;
import com.devteam.fantasy.util.BalanceType;
import com.devteam.fantasy.util.EstadoName;
import com.devteam.fantasy.util.HistoryEventType;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.PairNV;
import com.devteam.fantasy.util.SorteoTypeName;
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

	@Autowired
	EstadoRepository estadoRepository;
	
	
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
	@PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('SUPERVISOR')")
	public SorteosPasadosDays getSorteosPasadosCasaByWeek(Long weekId, String moneda) throws Exception {
		SorteosPasadosDays sorteosPasadosJugador	= new SorteosPasadosDays();
		try {
			logger.debug("getSorteosPasadosCasaByWeek(Long {},String {}): START", weekId,moneda);
			Week week 									= weekRepository.findById(weekId).orElseThrow(() -> new NotFoundException("Not Week Found"));
			
			List<Sorteo> sorteos 						= sorteoRepository.findAllBySorteoTimeBetweenAndEstadoOrderBySorteoTime(week.getMonday(),week.getSunday(), estadoRepository.getEstadoByEstado(EstadoName.CERRADA));
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
			
			DayOfWeek currentDay = sorteos.get(0).getSorteoTime().toLocalDateTime().getDayOfWeek();
			
			for(int i=0; i<sorteos.size(); i++) {
				Sorteo sorteo								= sorteos.get(i);
				List<HistoricoBalance> historicoBalanceList = historicoBalanceRepository.findAllBySorteoTime(sorteo.getSorteoTime());
				BigDecimal historicoBalance 				= BigDecimal.ZERO;
				
				for(HistoricoBalance hb: historicoBalanceList) {
					double currencyExchange = MathUtil.getDollarChangeRateOriginalMoneda(hb.getCambio(),hb.getMoneda().getMonedaName().toString(), moneda);
					BigDecimal balanceExchanged = BigDecimal.valueOf(hb.getBalance()).multiply(BigDecimal.valueOf(currencyExchange));
					historicoBalance = historicoBalance.add(balanceExchanged);
					
					logger.debug(hb.getJugador().getUsername()+" - "+hb.getBalance());
					logger.debug("historicoBalance: "+historicoBalance.doubleValue());
				}
				logger.debug(sorteo.getSorteoTime()+" - "+prevBalance.doubleValue() );
				prevBalance = historicoBalance.compareTo(BigDecimal.ZERO)!=0?prevBalance.add(historicoBalance):prevBalance;
				logger.debug("PrevBalance - "+prevBalance.doubleValue() );
				
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
				if(numeroGanador != null) {	
					if(sorteoTime.getHour() == 11) {
						sorteo11 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"11 am");
					}else if(sorteoTime.getHour() == 12) {
						sorteo12 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"12 pm");
					} else if(sorteoTime.getHour() == 15) {
						sorteo15 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"3 pm");
					}else if(sorteoTime.getHour() == 21) {
						sorteo21 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"9 pm");
					}
				}
				
				if( (i+1 != sorteos.size() && currentDay.compareTo(sorteos.get(i+1).getSorteoTime().toLocalDateTime().getDayOfWeek())<0)
						|| i+1 == sorteos.size()) {
					
					currentDay = i+1 == sorteos.size()
							? null
							:sorteos.get(i+1).getSorteoTime().toLocalDateTime().getDayOfWeek();
				
					
					SummaryResponse summaryDay = new SummaryResponse();
					summaryDay.setComisiones(comisionDay.doubleValue());
					summaryDay.setCurrency(summarySorteo.getCurrency());
					summaryDay.setPremios(premiosDay.doubleValue());
					summaryDay.setVentas(ventasDay.doubleValue());
					summaryDay.setSubTotal(subTotalDay.doubleValue());
					summaryDay.setPerdidasGanas(summaryDay.getSubTotal() -summaryDay.getPremios() - summaryDay.getBonos() );
					
					PairDayBalance sorteosPasado = new PairDayBalance();
					sorteosPasado.setSorteoTime(Util.getDayFromTimestamp(sorteo.getSorteoTime()));
//					sorteosPasado.setBalance(prevBalance.doubleValue());
					sorteosPasado.setBalance(summaryDay.getPerdidasGanas());
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
			for(Bono bono: bonoList) {
				double currencyExchange = MathUtil.getDollarChangeRateOriginalMoneda(bono.getCambio(),bono.getMoneda().getMonedaName().toString(), moneda);
				BigDecimal b= BigDecimal.valueOf(bono.getBono());
				b = b.multiply(BigDecimal.valueOf(currencyExchange));
				
				bonos = bonos.add(b);
			}
			
			
			summary.setBonos(bonos.doubleValue());
			summary.setComisiones(comisionWeek.doubleValue());
			summary.setPremios(premiosWeek.doubleValue());
			summary.setVentas(ventasWeek.doubleValue());
			summary.setSubTotal(subTotalWeek.doubleValue());
			summary.setCurrency(moneda);
			summary.setPerdidasGanas(summary.getSubTotal() -summary.getPremios() - summary.getBonos() );
			
			sorteosPasadosJugador.setSorteosPasados(pairDaysBalance);
			sorteosPasadosJugador.setSummary(summary);
			
		}catch (Exception e) {
			logger.error("getSorteosPasadosCasaByWeek(Long {},String {}): CATCH", weekId,moneda);
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			logger.debug("getSorteosPasadosCasaByWeek(Long weekID,String moneda): END");
		}
		return sorteosPasadosJugador;
	}
	
	@Override
	@PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('SUPERVISOR')")
	public SorteosPasadosJugadores getSorteosPasadosJugadoresByWeek(Long weekId, String moneda) throws Exception {
		SorteosPasadosJugadores result =  new SorteosPasadosJugadores();
		try {
			logger.debug("SorteosPasadosJugadores getSorteosPasadosJugadoresByWeek(Long {}, String {}): START", weekId,moneda);
			Week week 									= weekRepository.findById(weekId).orElseThrow(() -> new NotFoundException("Not Week Found"));
			List<Jugador> jugadores 					= jugadorRepository.findAllByOrderByIdAsc();
			List<JugadorBalanceWeek> jugadoresResponse 	= new ArrayList<JugadorBalanceWeek>();
			List<Bono> bonos 							= new ArrayList<>();
			
			logger.debug("Creating Jugadores List...");
			for(Jugador jugador : jugadores) {
				JugadorBalanceWeek jugadorWeek = new JugadorBalanceWeek(); 
				
				Optional<HistoricoBalance> balance = historicoBalanceRepository.findByBalanceTypeAndJugadorAndWeek(BalanceType.WEEKLY, jugador, week);
				
				if( !balance.isPresent()) {
					List<HistoricoBalance> weekBalances = historicoBalanceRepository.findAllByBalanceTypeAndJugadorAndWeekOrderById(BalanceType.BY_SORTEO, jugador, week); 
					Double weekBalanceTotal = weekBalances.stream().map(HistoricoBalance::getBalance).collect(Collectors.summarizingDouble(d->d)).getSum();
					
					if(weekBalances.size() > 0 ) {
						balance = Optional.of(weekBalances.get(weekBalances.size()-1));
						balance.get().setBalance(weekBalanceTotal);
					}
				}
				
				if( balance.isPresent()) {
					double currencyExchange = MathUtil.getDollarChangeRateOriginalMoneda(balance.get().getCambio(),balance.get().getMoneda().getMonedaName().toString(), jugador.getMoneda().getMonedaName().toString());
					BigDecimal balanceTotal = BigDecimal.valueOf(balance.get().getBalance()).multiply(BigDecimal.valueOf(currencyExchange));
					
					jugadorWeek.setBalance(balanceTotal.doubleValue());
				}
				
				List<Bono> bonoList = bonoRepository.findAllByWeekAndUser(week, jugador);
				if(bonoList != null && bonoList.size()>0) {
					jugadorWeek.setHaveBono(true);
					BigDecimal bonoJugador = BigDecimal.ZERO;
					
					for(Bono bono: bonoList) {
						bonoJugador = bonoJugador.add(BigDecimal.valueOf(bono.getBono()));
						bonos.add(bono);
					}
					
					jugadorWeek.setBalance(jugadorWeek.getBalance()+bonoJugador.doubleValue());
					
				}
				
				jugadorWeek.setId(jugador.getId());
				jugadorWeek.setName(jugador.getName());
				jugadorWeek.setUsername(jugador.getUsername());
				jugadorWeek.setMoneda(jugador.getMoneda().getMonedaName().toString());
				
				
				if (jugador.getUserState().equals(UserState.ACTIVE) || jugadorWeek.getBalance() != 0) {
					jugadoresResponse.add(jugadorWeek);
				}
				
			}
			
			logger.debug("Creating Week Summary...");
			List<HistoricoApuestas> apuestas = historicoApuestaRepository.findAllBySorteoSorteoTimeBetween(week.getMonday(),week.getSunday());
			SummaryResponse summarySorteo = sorteoTotales.processHitoricoApuestas(apuestas, moneda);
			
			BigDecimal bonoTotal = BigDecimal.ZERO;
			for(Bono bono: bonos) {
				double currencyExchange = MathUtil.getDollarChangeRateOriginalMoneda(bono.getCambio(),bono.getMoneda().getMonedaName().toString(), moneda);
				BigDecimal b= BigDecimal.valueOf(bono.getBono());
				b = b.multiply(BigDecimal.valueOf(currencyExchange));
				
				bonoTotal = bonoTotal.add(b);
			}
			
			summarySorteo.setBonos(bonoTotal.doubleValue());
			result.setJugadores(jugadoresResponse);
			result.setSummary(summarySorteo);
			
		} catch (Exception e) {
			logger.error("SorteosPasadosJugadores getSorteosPasadosJugadoresByWeek(Long {}, String {}): CATCH", weekId,moneda);
			logger.error(e.getMessage(), e);
			throw e;
		}finally {
			logger.debug("SorteosPasadosJugadores getSorteosPasadosJugadoresByWeek(Long weekID, String moneda): END");
		}
		
		return result;
	}
	
	@Override
	@PreAuthorize("hasRole('USER') or hasRole('ASIS')")
	public SorteosPasadosDays getSorteosPasadosJugadorByWeek(Long weekId, Long jugadorId) throws Exception {
		User user = userService.getById(jugadorId);
		return getSorteosPasadosJugadorByWeek(weekId, user);
	}
	
	@Override
	public SorteosPasadosDays getSorteosPasadosJugadorByWeek(Long weekId, User user) throws Exception {
		try {
			logger.debug("getSorteosPasadosJugadorByWeek(Long {},jugador {}): START", weekId,user.getId());
			Jugador jugador								= Util.getJugadorFromUser(user);
			Week week 									= weekRepository.findById(weekId).orElseThrow(() -> new NotFoundException("Not Week Found"));
			SorteosPasadosDays sorteosPasadosJugador 	= new SorteosPasadosDays();
			List<Sorteo> sorteos 						= sorteoRepository.findAllBySorteoTimeBetweenOrderBySorteoTimeWithNumeroGanadorNotNull(week.getMonday(),week.getSunday());
			List<PairDayBalance> pairDaysBalance 		= new ArrayList<>();
			
			boolean requestedByAdmin = userService.isUserAdminRole(userService.getLoggedInUser());
			
			
			
			BigDecimal comisionWeek 	= BigDecimal.ZERO;
			BigDecimal premiosWeek 		= BigDecimal.ZERO;
			BigDecimal ventasWeek 		= BigDecimal.ZERO;
			BigDecimal subTotalWeek	 	= BigDecimal.ZERO;
			BigDecimal comisionDay 		= BigDecimal.ZERO;
			BigDecimal premiosDay 		= BigDecimal.ZERO;
			BigDecimal ventasDay 		= BigDecimal.ZERO;
			BigDecimal subTotalDay		= BigDecimal.ZERO;
			double prevBalance 			= 0d; 

			SorteoNumeroGanador sorteo11 = null;
			SorteoNumeroGanador sorteo12 = null;
			SorteoNumeroGanador sorteo15 = null;
			SorteoNumeroGanador sorteo21 = null;
			
			
			DayOfWeek currentDay = sorteos.get(0).getSorteoTime().toLocalDateTime().getDayOfWeek();
			
			for(int i=0; i<sorteos.size(); i++) {
				Sorteo sorteo						= sorteos.get(i);
				HistoricoBalance historicoBalance	= historicoBalanceRepository.findBySorteoTimeAndJugadorAndBalanceType(sorteo.getSorteoTime(), jugador, BalanceType.BY_SORTEO);
				LocalDateTime sorteoTime			= sorteo.getSorteoTime().toLocalDateTime();
				prevBalance 						= historicoBalance!=null?prevBalance+historicoBalance.getBalance():prevBalance;
				
				List<HistoricoApuestas> apuestas = new ArrayList<>();
				if(user instanceof Jugador) {
					apuestas =  historicoApuestaRepository.findAllBySorteoAndUser(sorteo, user);
				}if (user instanceof Asistente) {
					apuestas  = historicoApuestaRepository.findAllBySorteoAndAsistente(sorteo, user);
				}

				if ( apuestas == null ) {
					continue;
				}
				
				SummaryResponse summarySorteo = sorteoTotales.processHitoricoApuestas(apuestas, jugador.getMoneda().getMonedaName().toString());

				comisionWeek	= comisionWeek.add( new BigDecimal(summarySorteo.getComisiones()));
				premiosWeek		= premiosWeek.add(new BigDecimal(summarySorteo.getPremios()));
				ventasWeek	 	= ventasWeek.add(new BigDecimal(summarySorteo.getVentas()));
				subTotalWeek	= subTotalWeek.add(new BigDecimal(summarySorteo.getSubTotal()));
				comisionDay	= comisionDay.add( new BigDecimal(summarySorteo.getComisiones()));
				premiosDay	= premiosDay.add(new BigDecimal(summarySorteo.getPremios()));
				ventasDay	= ventasDay.add(new BigDecimal(summarySorteo.getVentas()));
				subTotalDay	= subTotalDay.add(new BigDecimal(summarySorteo.getSubTotal()));
					
				NumeroGanador numeroGanador 		= numeroGanadorRepository.getBySorteo(sorteo);
				if(numeroGanador != null) {	
					if(sorteoTime.getHour() == 11) {
						sorteo11 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"11 am");
					}else if(sorteoTime.getHour() == 12) {
						sorteo12 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"12 pm");
					} else if(sorteoTime.getHour() == 15) {
						sorteo15 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"3 pm");
					}else if(sorteoTime.getHour() == 21) {
						sorteo21 = buildSorteoNumeroGanador(sorteo,numeroGanador.getNumeroGanador(),"9 pm");
					}
				}
					
				if( (i+1 != sorteos.size() && currentDay.compareTo(sorteos.get(i+1).getSorteoTime().toLocalDateTime().getDayOfWeek())<0)
						|| i+1 == sorteos.size()) {
					
					currentDay = i+1 == sorteos.size()
							? null
							:sorteos.get(i+1).getSorteoTime().toLocalDateTime().getDayOfWeek();
					
					SummaryResponse summaryDay = new SummaryResponse();
					summaryDay.setComisiones(comisionDay.doubleValue());
					summaryDay.setCurrency(jugador.getMoneda().getMonedaName().toString());
					summaryDay.setPremios(premiosDay.doubleValue());
					summaryDay.setVentas(ventasDay.doubleValue());
					summaryDay.setSubTotal(subTotalDay.doubleValue());
					summaryDay.setPerdidasGanas(summaryDay.getSubTotal() -summaryDay.getPremios() - summaryDay.getBonos() );
					
					if(requestedByAdmin) {
						summaryDay.setPerdidasGanas(summaryDay.getPerdidasGanas()* (-1));
					}
					
					PairDayBalance sorteosPasado = new PairDayBalance();
					sorteosPasado.setSorteoTime(Util.getDayFromTimestamp(sorteo.getSorteoTime()));
					sorteosPasado.setBalance(prevBalance);
					sorteosPasado.setSummary(summaryDay);
					pairDaysBalance.add(sorteosPasado);
					
					List<SorteoNumeroGanador> numerosGanadores = new ArrayList<>();
					if(sorteo11 != null)
						numerosGanadores.add(sorteo11);
					
					if(sorteoTime.getDayOfWeek() == DayOfWeek.SUNDAY && sorteo12 != null) 
						numerosGanadores.add(sorteo12);
					
					if(sorteo15 != null)
						numerosGanadores.add(sorteo15);
					
					if(sorteo21 != null)
						numerosGanadores.add(sorteo21);
					
					sorteosPasado.setSorteos(numerosGanadores);
					
					prevBalance = 0d;
					sorteo11 	= null;
					sorteo12 	= null;
					sorteo15 	= null;
					sorteo21 	= null;
					comisionDay = BigDecimal.ZERO;
					premiosDay 	= BigDecimal.ZERO;
					ventasDay 	= BigDecimal.ZERO;
					subTotalDay	= BigDecimal.ZERO;
				}
			}
			
			SummaryResponse summary = new SummaryResponse();
			List<Bono> bonos = bonoRepository.findAllByWeekAndUser(week, jugador);
			if( bonos !=null && bonos.size()>0) {
				BigDecimal bonoTotla = BigDecimal.ZERO;
				for(Bono bono: bonos) {
					bonoTotla = bonoTotla.add(BigDecimal.valueOf(bono.getBono()));
				}
				summary.setBonos(bonoTotla.doubleValue());
			}
			
			summary.setComisiones(comisionWeek.doubleValue());
			summary.setPremios(premiosWeek.doubleValue());
			summary.setVentas(ventasWeek.doubleValue());
			summary.setSubTotal(subTotalWeek.doubleValue());
			summary.setCurrency(jugador.getMoneda().getMonedaName().toString());
			summary.setPerdidasGanas(summary.getSubTotal() -summary.getPremios() - summary.getBonos() );
			
			if( !requestedByAdmin) {
				summary.setPerdidasGanas(summary.getPerdidasGanas()* (-1));
			}
			
			sorteosPasadosJugador.setSorteosPasados(pairDaysBalance);
			sorteosPasadosJugador.setSummary(summary);
			
			return sorteosPasadosJugador;
		}catch (Exception e) {
			logger.debug("getSorteosPasadosJugadorByWeek(Long {},jugador {}): CATCH", weekId,user.getId());
			logger.error(e.getMessage(), e);
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
		
		Timestamp currentDate = Timestamp.valueOf(LocalDateTime.now()); 
		
		weeks.forEach(week -> {
			
			String monday = "";
			String sunday = "";
//			if( week.getMonday().compareTo(currentDate) >= 0 ) {
//				monday = "Hoy";
//			}else {
				monday = Util.getShortDayFromTimestamp(week.getMonday());
//			}
//			
//			if(week.getMonday().compareTo(currentDate) == 0) {
//				sunday = "";
//			}else if(week.getSunday().compareTo(currentDate) > 0) {
//				sunday = "Presente";
//			}else {
				sunday = Util.getShortDayFromTimestamp(week.getSunday());
//			}
			
			WeekResponse w = new WeekResponse();
			w.setId(week.getId());
			w.setYear(week.getYear());
			w.setMonday(monday);
			w.setSunday(sunday);
			response.add(w);
		});
		return response;
	}
	
	@Override
	public void validateIfJugadorIsElegibleForBono(Jugador jugador, Week week, Bono bono) throws CanNotInsertBonoException, NotFoundException {
		HistoricoBalance weekBalance = historicoBalanceRepository
				.findByBalanceTypeAndJugadorAndWeek(BalanceType.WEEKLY,jugador,week)
				.orElseThrow(() -> new NotFoundException("El jugador no tiene balance de cierre de semana"));
		
		if(weekBalance.getBalance()>=0) {
			throw new CanNotInsertBonoException(week.getId(), jugador.getId(), "Jugador not elegible for bono");
		}else if(bono.getBono() >= (weekBalance.getBalance() * (-1))) {
			throw new CanNotInsertBonoException(week.getId(), jugador.getId(), "Bono must be less than weekly balance");
		}
		
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
	public SorteosPasadosApuestas getApuestasPasadasBySorteoAndJugador(Long sorteoId, User user) throws Exception {
		SorteosPasadosApuestas sorteosPasadosApuestas = new SorteosPasadosApuestas();
		Sorteo sorteo;
		
		try {
			logger.debug("getApuestasPasadasBySorteoAndJugador(Long {}, Jugador {}): START", sorteoId, user.getId());
			Jugador jugador = Util.getJugadorFromUser(user);
			MonedaName currencyRequested =null;
			sorteo = sorteoRepository.findById(sorteoId).orElseThrow(() -> new NotFoundException("Sorteo not found with id: "+sorteoId));
			
			List<HistoricoApuestas> apuestas = new ArrayList<>();
			if( user instanceof Jugador) {
				apuestas = historicoApuestaRepository.findAllBySorteoAndUser(sorteo, user);
				currencyRequested = jugador.getMoneda().getMonedaName();
			}else if( user instanceof Asistente){
				apuestas = historicoApuestaRepository.findAllBySorteoAndAsistente(sorteo, user);
				currencyRequested = jugador.getMoneda().getMonedaName();
			}else {
				apuestas = historicoApuestaRepository.findAllBySorteo(sorteo);
				currencyRequested = MonedaName.LEMPIRA;
			}
			
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
			SummaryResponse summary = sorteoTotales.processHitoricoApuestas(apuestas, currencyRequested.toString());
			sorteosPasadosApuestas.setxApuestas(hasApuestasMadeByAsistente);
			sorteosPasadosApuestas.setApuestas(pairs);
			sorteosPasadosApuestas.setSummary(summary);
		} catch (Exception e) {
			logger.error("getApuestasPasadasBySorteoAndJugador(Long {}, Jugador {}): CATCH", sorteoId, user.getId());
			logger.error(e.getMessage(), e);
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
				numeroResponse.setSorteoId(numero.getSorteo().getId());
				numeroResponse.setSorteoType(numero.getSorteo().getSorteoType().getSorteoTypeName().toString());
				
				numeroResponse.setJugadores(premios);
				numeroResponse.setPremio(premioSorteoTotal.doubleValue());
				numeroResponse.setDay(Util.getDayFromTimestamp(numero.getSorteo().getSorteoTime()));
				numeroResponse.setHour(Util.getHourFromTimestamp(numero.getSorteo().getSorteoTime()));
				
				numerosGanadoresResponse.add(numeroResponse);
			}
		} catch (Exception e) {
			logger.error("List<NumeroGanadorSorteoResponse> getNumerosGanadores(): CATCH");
			logger.error(e.getMessage(),e);
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
            
            List<Asistente> asistentes = asistenteRepository.findAllByJugadorAndUserState(jugador, UserState.ACTIVE);
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
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			logger.debug("List<HistoricoApuestaDetallesResponse> getHistoricoApuestaDetallesX(Long id, User user): END");
		}
        
        return apuestasDetails;
	}

	@Override
	public HistoricoBalance getWeekBalanceByJugador(Jugador jugador) {
		HistoricoBalance result = historicoBalanceRepository.findByJugadorAndBalanceType(jugador, BalanceType.WEEKLY);
		return result;
	}

}


















