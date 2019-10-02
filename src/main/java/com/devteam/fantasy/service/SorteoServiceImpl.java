package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devteam.fantasy.exception.CanNotInsertApuestaException;
import com.devteam.fantasy.exception.CanNotInsertHistoricoBalanceException;
import com.devteam.fantasy.exception.CanNotInsertWinningNumberException;
import com.devteam.fantasy.exception.CanNotRemoveApuestaException;
import com.devteam.fantasy.exception.InvalidSorteoStateException;
import com.devteam.fantasy.exception.SorteoEstadoNotValidException;
import com.devteam.fantasy.math.MathUtil;
import com.devteam.fantasy.math.SorteoTotales;
import com.devteam.fantasy.message.response.ApuestaActivaResponse;
import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorSorteosResponse;
import com.devteam.fantasy.message.response.NumeroPlayerEntryResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.message.response.SorteosPasadosJugadores;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Cambio;
import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.NumeroGanador;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.repository.ApuestaRepository;
import com.devteam.fantasy.repository.AsistenteRepository;
import com.devteam.fantasy.repository.CambioRepository;
import com.devteam.fantasy.repository.EstadoRepository;
import com.devteam.fantasy.repository.HistoricoApuestaRepository;
import com.devteam.fantasy.repository.HistoricoBalanceRepository;
import com.devteam.fantasy.repository.JugadorRepository;
import com.devteam.fantasy.repository.NumeroGanadorRepository;
import com.devteam.fantasy.repository.ResultadoRepository;
import com.devteam.fantasy.repository.SorteoDiariaRepository;
import com.devteam.fantasy.repository.SorteoRepository;
import com.devteam.fantasy.repository.SorteoTypeRepository;
import com.devteam.fantasy.repository.UserRepository;
import com.devteam.fantasy.repository.WeekRepository;
import com.devteam.fantasy.util.ApostadorName;
import com.devteam.fantasy.util.BalanceType;
import com.devteam.fantasy.util.ChicaName;
import com.devteam.fantasy.util.EstadoName;
import com.devteam.fantasy.util.HistoryEventType;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.PairNV;
import com.devteam.fantasy.util.SorteoTypeName;
import com.devteam.fantasy.util.TuplaRiesgo;
import com.devteam.fantasy.util.Util;

@Service
public class SorteoServiceImpl implements SorteoService {

	@Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	SorteoRepository sorteoRepository;

	@Autowired
	SorteoDiariaRepository sorteoDiariaRepository;

	@Autowired
	ApuestaRepository apuestaRepository;

	@Autowired
	EstadoRepository estadoRepository;

	@Autowired
	SorteoTypeRepository sorteoTypeRepository;

	@Autowired
	AsistenteRepository asistenteRepository;

	@Autowired
	JugadorRepository jugadorRepository;

	@Autowired
	CambioRepository cambioRepository;

	@Autowired
	HistoricoApuestaRepository historicoApuestaRepository;
	
	@Autowired
	HistoricoBalanceRepository historicoBalanceRepository;

	@Autowired
	ResultadoRepository resultadoRepository;

	@Autowired
	NumeroGanadorRepository numeroGanadorRepository;

	@Autowired
	SorteoTotales sorteoTotales;

	@Autowired
	HistoryService historyService;
	
	@Autowired
	WeekRepository weekRepository;

	private static final Logger logger = LoggerFactory.getLogger(SorteoServiceImpl.class);

	/**
	 * Type [Diaria] [Sorteos] must be sort by time, but keeping the original
	 * position of [Sorteo] [Chica]
	 * @throws Exception 
	 */
	public List<SorteoDiaria> getActiveSorteosList() throws Exception {
		List<SorteoDiaria> result = null;
		try {
			logger.debug("getActiveSorteosList(): START");
			result = getActiveSorteosList(null);
		} catch (Exception e) {
			logger.error("getActiveSorteosList(): START");
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("getActiveSorteosList(): END");
		}
		return result;
	}

	public List<SorteoDiaria> getActiveSorteosList(User user) throws Exception {
		List<SorteoDiaria> sorteos = null;
		try {
			logger.debug("getActiveSorteosList(User {}): START", user);
			sorteos =getSortDiariaList(); 

			for (SorteoDiaria sorteoDiaria : sorteos) {
				Set<Apuesta> apuestas = null;
				if (user == null) {
					apuestas = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);
				} else {
					apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
				}
				sorteoDiaria.setApuestas(apuestas);
			}
		} catch (Exception e) {
			logger.error("getActiveSorteosList(User {}): CATCH", user);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("getActiveSorteosList(User user): END");
		}
		return sorteos;
	}

	public ApuestaActivaResumenResponse getActiveSorteoDetail(Long id, String currency) {
		ApuestaActivaResumenResponse result = null;
		try {
			logger.debug("getActiveSorteoDetail(Long {}, String {}): START", id, currency);
			List<TuplaRiesgo> tuplaRiesgos = new ArrayList<>();
			SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
			Sorteo sorteo = sorteoDiaria.getSorteo();
			Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);

			double[] cantidad = new double[100];
			double[] riesgo = new double[100];
			final double[] total = { 0.0 };
			final double[] comision = { 0.0 };

			apuestaList.forEach(apuesta -> {
				int numero = apuesta.getNumero();
				Jugador jugador = Util.getJugadorFromApuesta(apuesta);
				calcularCantRiesgo(sorteo, cantidad, riesgo, apuesta, numero, jugador, total, currency, comision);
			});

			double max = 0.0;
			int pos = -1;
			double totalValue = 0;
			for (int i = 0; i < 100; i++) {
				if (cantidad[i] != 0) {
					TuplaRiesgo tuplaRiesgo = new TuplaRiesgo();
					tuplaRiesgo.setNumero(i);
					tuplaRiesgo.setDineroApostado(cantidad[i]);
					tuplaRiesgo.setTotalRiesgo(riesgo[i]);
					tuplaRiesgos.add(tuplaRiesgo);
					totalValue += cantidad[i];
					if (max < riesgo[i]) {
						max = riesgo[i];
						pos = i;
					}
				}
			}

			TuplaRiesgo tuplaRiesgo = new TuplaRiesgo();
			if (pos != -1) {
				tuplaRiesgo.setNumero(pos);
				tuplaRiesgo.setDineroApostado(cantidad[pos]);
				tuplaRiesgo.setTotalRiesgo(riesgo[pos]);
			}
			result = new ApuestaActivaResumenResponse(tuplaRiesgo, tuplaRiesgos, comision[0], totalValue);

		} catch (Exception e) {
			logger.error("getActiveSorteoDetail(Long {}, String {}): CATCH", id, currency);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("getActiveSorteoDetail(Long id, String currency): END");
		}

		return result;
	}

	private List<SorteoDiaria> getSortDiariaList() throws Exception {
		List<SorteoDiaria> sorteos = sorteoDiariaRepository.findAllByOrderBySorteoTime();
		try {
			logger.debug("sortDiariaList(): START", sorteos);
			if(sorteos.size()!=4) {
				throw new Exception("Sorteos list should contian only 4 sorteos");
			}
			
			int newChicaIndex = 1;
			int currentChicaIndex = IntStream.range(0, sorteos.size()).filter(
					i -> SorteoTypeName.CHICA.equals(sorteos.get(i).getSorteo().getSorteoType().getSorteoTypeName()))
					.findFirst().getAsInt();
			
			SorteoDiaria sorteoChica = sorteos.get(currentChicaIndex);
			LocalDateTime sorteoTimeChica =  sorteoChica.getSorteoTime().toLocalDateTime();
			sorteos.remove(currentChicaIndex);
			
			boolean fullOfSundaySorteos = true;
			
			for(SorteoDiaria sorteo: sorteos) {
				if(sorteo.getSorteoTime().toLocalDateTime().getDayOfWeek().compareTo(DayOfWeek.SUNDAY) != 0) {
					fullOfSundaySorteos = false;
					break;
				}
			}
			
			if( fullOfSundaySorteos ) {
				newChicaIndex = 3;
			}else {
				
				LocalDateTime sorteoTimePos0 = sorteos.get(0).getSorteoTime().toLocalDateTime();
				LocalDateTime sorteoTimePos1 = sorteos.get(1).getSorteoTime().toLocalDateTime();
				LocalDateTime sorteoTimePos2 = sorteos.get(2).getSorteoTime().toLocalDateTime();
				
				if (sorteoTimePos0.getDayOfWeek().compareTo(sorteoTimePos1.getDayOfWeek()) == 0
						&& sorteoTimePos0.getDayOfMonth() == sorteoTimePos1.getDayOfMonth()) {
					newChicaIndex++;
				}
				if (sorteoTimePos0.getDayOfWeek().compareTo(sorteoTimePos2.getDayOfWeek()) == 0
						&& sorteoTimePos0.getDayOfMonth() == sorteoTimePos2.getDayOfMonth()) {
					newChicaIndex++;
				}
			}
			
			sorteos.add(newChicaIndex,sorteoChica);

		} catch (Exception e) {
			logger.error("sortDiariaList(): CATCH");
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("sortDiariaList(): END");
		}
		return sorteos;
	}

	/**
	 * Make sure to only pass the "sorteos" for the passed user, this meaning the
	 * list of sorteos should not include the asistente sorteos. ex. sorteos =
	 * apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
	 */
	public List<SorteoResponse> getSorteosResponses(List<SorteoDiaria> sorteos, User user) {
		List<SorteoResponse> sorteoResponses = null;
		try {
			logger.debug("getSorteosResponses(List<SorteoDiaria> {}, User {}): START", sorteos, user);
			sorteoResponses = new ArrayList<>();
			Jugador jugador = Util.getJugadorFromUser(user);
			MonedaName moneda = jugador.getMoneda().getMonedaName();

			for (SorteoDiaria sorteoDiaria : sorteos) {
				sorteoTotales.processSorteo(jugador, sorteoDiaria);

				String estado = sorteoDiaria.getSorteo().getEstado().getEstado().toString();
				sorteoResponses.add(new SorteoResponse(sorteoDiaria.getId(),
						Util.formatTimestamp2String(sorteoDiaria.getSorteoTime()),
						Util.getDayFromTimestamp(sorteoDiaria.getSorteoTime()),
						Util.getHourFromTimestamp(sorteoDiaria.getSorteoTime()), sorteoTotales.getVentas(),
						sorteoTotales.getComisiones(), sorteoTotales.getTotal(), estado, moneda.toString(),
						sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString()));
			}
		} catch (Exception e) {
			logger.error("getSorteosResponses(List<SorteoDiaria> {}, User {}): CATCH", sorteos, user);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("getSorteosResponses(List<SorteoDiaria> sorteos, User user): END");
		}
		return sorteoResponses;
	}

	private void calcularCantRiesgo(Sorteo sorteo, double[] cantidad, double[] riesgo, Apuesta apuesta, int numero,
			Jugador jugador, double[] total, String currency, double[] comision) {

		// TODO needs to use Util.getPremioMultiplier and sorteoTotales methods to get
		// costomultiplier and comisionRate
		try {
			logger.debug("calcularCantRiesgo(...): START");
			BigDecimal cambio = Util.getApuestaCambio(currency, apuesta);
			BigDecimal premio = BigDecimal.ZERO;

			BigDecimal costoMilChica = BigDecimal.ONE;
			BigDecimal costoMilDiaria = BigDecimal.ONE;
			BigDecimal costoPedazoChica = BigDecimal.ONE;

			if (Util.isSorteoTypeDiaria(sorteo)) {
				if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.DIRECTO)) {
					premio = BigDecimal.valueOf(jugador.getPremioDirecto())
							.multiply(BigDecimal.valueOf(apuesta.getCantidad())).multiply(cambio);
				} else {
					premio = BigDecimal.valueOf(jugador.getPremioMil())
							.multiply(BigDecimal.valueOf(apuesta.getCantidad())).multiply(cambio);
				}

				costoMilDiaria = jugador.getCostoMil() != 0 ? BigDecimal.valueOf(jugador.getCostoMil())
						: BigDecimal.ONE;

				cantidad[numero] += BigDecimal.valueOf(apuesta.getCantidad()).multiply(cambio).multiply(costoMilDiaria)
						.doubleValue();
				comision[0] += cambio.multiply(BigDecimal.valueOf(apuesta.getComision())).doubleValue();
				riesgo[numero] += premio.doubleValue();

			} else {
				if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
					premio = BigDecimal.valueOf(jugador.getPremioChicaDirecto())
							.multiply(BigDecimal.valueOf(apuesta.getCantidad())).multiply(cambio);

				} else if (jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)) {
					premio = BigDecimal.valueOf(jugador.getPremioChicaMiles())
							.multiply(BigDecimal.valueOf(1000).multiply(BigDecimal.valueOf(apuesta.getCantidad())))
							.multiply(cambio);

				} else {
					premio = BigDecimal.valueOf(jugador.getPremioChicaPedazos())
							.multiply(BigDecimal.valueOf(apuesta.getCantidad())).multiply(cambio);
				}

				costoPedazoChica = jugador.getCostoChicaPedazos() != 0
						? BigDecimal.valueOf(jugador.getCostoChicaPedazos())
						: BigDecimal.ONE;
				costoMilChica = jugador.getCostoChicaMiles() != 0 ? BigDecimal.valueOf(jugador.getCostoChicaMiles())
						: BigDecimal.ONE;

				cantidad[numero] += BigDecimal.valueOf(apuesta.getCantidad()).multiply(cambio).multiply(costoMilChica)
						.multiply(costoPedazoChica).doubleValue();

				comision[0] += BigDecimal.valueOf(apuesta.getComision()).multiply(cambio).doubleValue();
				riesgo[numero] += premio.doubleValue();
			}
			total[0] += premio.doubleValue();
		} catch (Exception e) {
			logger.error("calcularCantRiesgo(...): CATCH");
			logger.error(
					"sorteo= {}, cantidad[]= {}, riesgo[]={}, Apuesta={}, Numero={}, Jugador={}, total={}, currency={}, comision={}:",
					sorteo, Arrays.toString(cantidad), Arrays.toString(riesgo), apuesta, numero, jugador,
					Arrays.toString(total), currency, Arrays.toString(comision));
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("calcularCantRiesgo(...): END");
		}
	}

	@Override
	public List<ApuestasActivasResponse> getSorteosListWithMoneda(String currency) throws Exception {
		List<ApuestasActivasResponse> apuestasActivasResponses = new ArrayList<>();
		try {
			logger.debug("getSorteosListWithMoneda(String {}): START", currency);
			List<SorteoDiaria> sorteoDiarias = getActiveSorteosList();
			sorteoDiarias.forEach(sorteoDiaria -> {
				apuestasActivasResponses.add(getApuestasActivasResponse(sorteoDiaria, currency));
			});
		} catch (Exception e) {
			logger.error("getSorteosListWithMoneda(String {}): CATCH", currency);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("getSorteosListWithMoneda(String currency): END");
		}
		return apuestasActivasResponses;
	}

	private ApuestasActivasResponse getApuestasActivasResponse(SorteoDiaria sorteoDiaria, String currency) {

		BigDecimal total = BigDecimal.ZERO;
		BigDecimal comision = BigDecimal.ZERO;
		BigDecimal premio = BigDecimal.ZERO;
		BigDecimal neta = BigDecimal.ZERO;

		ApuestasActivasResponse activaResponse = new ApuestasActivasResponse();

		try {
			logger.debug("getApuestasActivasResponse( SorteoDiaria {}, String {}): START", sorteoDiaria, currency);
			MonedaName moneda = currency.equalsIgnoreCase(MonedaName.LEMPIRA.toString()) ? MonedaName.LEMPIRA
					: MonedaName.DOLAR;

			sorteoTotales.processSorteo(null, sorteoDiaria, moneda, true);
			total = total.add(sorteoTotales.getVentasBD());
			comision = comision.add(sorteoTotales.getComisionesBD());

			neta = total.subtract(comision);

			activaResponse.setTotal(total.doubleValue());
			activaResponse.setComision(comision.doubleValue());
			activaResponse.setNeta(neta.doubleValue());
			activaResponse.setPremio(premio.doubleValue());
			activaResponse.setBalance(neta.subtract(premio).doubleValue());
			activaResponse.setId(sorteoDiaria.getSorteo().getId());
			activaResponse.setTitle(Util.formatTimestamp2String(sorteoDiaria.getSorteo().getSorteoTime()));
			activaResponse.setEstado(sorteoDiaria.getSorteo().getEstado().getEstado().toString());
			activaResponse.setType(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString());

		} catch (Exception e) {
			logger.error("getApuestasActivasResponse( SorteoDiaria {}, String {}): CATCH", sorteoDiaria, currency);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("getApuestasActivasResponse( SorteoDiaria sorteoDiaria, String currency): END");
		}
		return activaResponse;
	}

	public JugadorSorteosResponse getJugadorList() throws Exception {
		JugadorSorteosResponse jugadorSorteosResponse = new JugadorSorteosResponse();

		try {
			logger.debug("getJugadorList(): START");
			User user = userService.getLoggedInUser();
			Jugador jugador = Util.getJugadorFromUser(user);
			jugadorSorteosResponse.setName(user.getName());
			jugadorSorteosResponse.setMoneda(jugador.getMoneda().getMonedaName().toString());

			List<SorteoDiaria> sorteos = getSortDiariaList();
			jugadorSorteosResponse.setSorteos(getSorteosResponses(sorteos, user));

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("getJugadorList(): END");
		}
		return jugadorSorteosResponse;

	}

	/*PreCondition:
	 * If sorteotime is Sunday 9pm, all the sorteos that belong to the week must be closed 
	 * and have a winning number
	 * 
	 * Steps
	 * 1) Set NumeroGanador.
	 * 2) Create History Event for Winning Number.
	 * 3) Collect all the Users who have bet to the winning number.
	 * 4) Reset User's balance to Zero.
	 * 5) Create History Balance.
	 * 6) Copy all bets to history Bets.
	 * 7) Delete sorteoDiaria and create the next sorteo Diaria with the same time.
	 * 8) If is the last sorteoDiaria of the week close the week.
	 * 8.1) Save week balance in history
	 * 8.2) Create week model
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void setNumeroGanador(Long id, int numero) throws CanNotInsertWinningNumberException, CanNotInsertHistoricoBalanceException  {
		try {
			logger.debug("setNumeroGanador(Long {}, int {}): START", id, numero);
			logger.info("setNumeroGanador(Long {}, int {}): START", id, numero);
			User loggedUser = userService.getLoggedInUser();
			Cambio currentCambio = cambioRepository.findFirstByOrderByIdDesc();
			SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
			Sorteo sorteo = sorteoDiaria.getSorteo();
			
			logger.debug("validateWinningNumberPreCondition()");
			logger.info("validateWinningNumberPreCondition()");
			validateWinningNumberPreCondition(sorteo);
			
			NumeroGanador numeroGanador = new NumeroGanador();
			numeroGanador.setNumeroGanador(numero);
			numeroGanador.setSorteo(sorteo);
			numeroGanadorRepository.save(numeroGanador);
			historyService.createEvent(HistoryEventType.WINNING_NUMBER, id, "", String.valueOf(numero));
			
			Week week = getWeekFromSorteoTime(sorteo.getSorteoTime());
			
			logger.debug("numeroGanadorRepository.save({})", numeroGanador);
			
			Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);
			// Long [jugadorId], Integer [unidadesApostadas]
			Map<Long, Integer> map = new HashMap<>();
			
			//Ids for logging a few lines below
			List<Long> jugadorIds = new ArrayList<>();
			
			for (Apuesta apuesta : apuestas) {
				Jugador jugador = Util.getJugadorFromApuesta(apuesta);
				Integer cantidadActual = Optional.ofNullable(map.get(jugador.getId())).orElse(0);
				
				if(apuesta.getNumero() == numero) {
					cantidadActual += apuesta.getCantidad().intValue();
				}
				
				if (!map.containsKey(jugador.getId()) ) jugadorIds.add(jugador.getId());
				map.put(jugador.getId(), cantidadActual);
			}

			logger.debug("update balance for jugadores by id:{}", jugadorIds);
			/*
			 * New balance will be determine by premio of a bet.
			 * then the premio will be added or substract of the total of bets made for the sorteo
			 * the result will be added or substract from the jugador current balance.
			 */
			Set<Entry<Long, Integer>> jugadores = map.entrySet();
			Iterator<Entry<Long, Integer>> jugadoresIterator = jugadores.iterator();
			
			while(jugadoresIterator.hasNext()) {
				Map.Entry<Long, Integer> jugadorApuestasGanadas = (Map.Entry<Long, Integer>)jugadoresIterator.next();
				Jugador jugador = jugadorRepository.findById(jugadorApuestasGanadas.getKey()).get();
				BigDecimal premioMultiplier = MathUtil.getPremioMultiplier(jugador,sorteo.getSorteoType().getSorteoTypeName());
				BigDecimal premio = BigDecimal.valueOf(jugadorApuestasGanadas.getValue()).multiply(premioMultiplier);

				sorteoTotales.processSorteo(jugador, sorteoDiaria);
				BigDecimal totalApuestas = sorteoTotales.getTotalBD();
				BigDecimal result = premio.subtract(totalApuestas);
				
				BigDecimal newBalance = BigDecimal.valueOf(jugador.getBalance()).add(result);
				jugador.setBalance(newBalance.doubleValue());
				jugadorRepository.save(jugador);
				createHistoricoBalance(loggedUser,currentCambio,jugador, BalanceType.DAILY ,sorteoDiaria.getSorteoTime(), week);
			}
			
			copyApuestasToHistoricoApuestas(sorteoDiaria);
			deleteAndCreateSorteoDiaria(sorteoDiaria);

			if (Util.isSorteoTypeDiaria(sorteoDiaria.getSorteo())
					&& Util.getDayOfWeekFromTimestamp(sorteoDiaria.getSorteo().getSorteoTime()).equals(DayOfWeek.SUNDAY)
					&& Util.getlocalDateTimeHourFromTimestamp(sorteoDiaria.getSorteo().getSorteoTime()) == 21) {
				logger.debug("Cerrar Semana");
            	cerrarSemana(sorteoDiaria, week);
			}
		} catch (CanNotInsertWinningNumberException cniwne) {
			logger.error("setNumeroGanador(Long {}, int {}): CATCH", id, numero);
			logger.error(cniwne.getMessage());
			throw cniwne;
		} catch (CanNotInsertHistoricoBalanceException cnihbe) {
			logger.error("setNumeroGanador(Long {}, int {}): CATCH", id, numero);
			logger.error(cnihbe.getMessage());
			cnihbe.printStackTrace();
			throw cnihbe;
		} finally {
			logger.debug("setNumeroGanador(Long id, int numero): END");
			logger.info("setNumeroGanador(Long id, int numero): END");
		}
	}

	private void validateWinningNumberPreCondition(Sorteo sorteo) throws CanNotInsertWinningNumberException {
		LocalDateTime sorteoTime = sorteo.getSorteoTime().toLocalDateTime();
		
		if(sorteoTime.getDayOfWeek() == DayOfWeek.SUNDAY && sorteoTime.getHour() == 21) {
			List<SorteoDiaria> sorteosDiaria = sorteoDiariaRepository.findAllByOrderById();
			
			for(SorteoDiaria sorteoDiaria: sorteosDiaria) {
				
				if(sorteo.getSorteoTime().compareTo(sorteoDiaria.getSorteoTime())!= 0
						&& (sorteoDiaria.getSorteoTime().toLocalDateTime().getDayOfWeek() == DayOfWeek.SUNDAY 
							&& sorteoDiaria.getSorteoTime().toLocalDateTime().getDayOfMonth() == sorteoTime.getDayOfMonth())) {
					throw new CanNotInsertWinningNumberException("Este es el ultimo sorteo de la semana, verificar que no hay ningun otro sorteo faltante de numero ganador para esta semana.");
				}
			}
		}
		
	}

	@Override
	@Transactional(rollbackFor = CanNotInsertHistoricoBalanceException.class)
	public void cerrarSemana(SorteoDiaria sorteoDiaria, Week week) throws CanNotInsertHistoricoBalanceException {
		try {
			User loggedUser = userService.getLoggedInUser();
			Cambio currentCambio = cambioRepository.findFirstByOrderByIdDesc();
			
			Set<Jugador> jugadoresWithBalance = jugadorRepository.findAllByBalanceNot(0d);
			Long weekId = null;
			for(Jugador jugador:jugadoresWithBalance){
				try {
					HistoricoBalance historicoBalance = createHistoricoBalance(loggedUser, currentCambio, jugador, BalanceType.WEEKLY ,sorteoDiaria.getSorteoTime(), week);
					jugador.setBalance(0);
					jugadorRepository.save(jugador);
					
					if(weekId == null) {
						weekId = historicoBalance.getWeek().getId();
					}
				}catch(CanNotInsertHistoricoBalanceException hbe) {
					throw new CanNotInsertHistoricoBalanceException(hbe, jugador, sorteoDiaria);
				}
			};
			historyService.createEvent(HistoryEventType.WEEK_CLOSED, weekId);
		}catch (CanNotInsertHistoricoBalanceException hbe) {
			throw hbe;
		}
	}

	private HistoricoBalance createHistoricoBalance(User loggedUser, Cambio currentCambio, Jugador jugador, BalanceType balanceType, Timestamp sorteoTime, Week week) throws CanNotInsertHistoricoBalanceException {
		HistoricoBalance historico = null;
		try {
			historico = new HistoricoBalance();
			historico.setBalanceSemana(jugador.getBalance());
			historico.setJugador(jugador);
			historico.setCreatedBy(loggedUser);
			historico.setSorteoTime(sorteoTime);
			historico.setMoneda(jugador.getMoneda());
			historico.setCambio(currentCambio);
			historico.setBalanceType(balanceType);
			historico.setWeek(week);
			historicoBalanceRepository.save(historico);
		}catch (Exception e) {
			throw new CanNotInsertHistoricoBalanceException(e);
		}
		return historico;
	}

	@Transactional(rollbackFor = Exception.class)
	private void copyApuestasToHistoricoApuestas(SorteoDiaria sorteoDiaria) {
		try {
			logger.debug("copyApuestasToHistoricoApuestas(SorteoDiaria {}): START", sorteoDiaria);
			Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);
			apuestaList.forEach(apuesta -> {
				HistoricoApuestas historicoApuestas = new HistoricoApuestas();
				Jugador jugador = Util.getJugadorFromApuesta(apuesta);
				
				historicoApuestas.setUser(jugador);
				if( apuesta.getUser() instanceof Asistente) {
					historicoApuestas.setAsistente(apuesta.getUser());
				}
				
				historicoApuestas.setCantidad(apuesta.getCantidad());
				historicoApuestas.setSorteo(sorteoDiaria.getSorteo());
				historicoApuestas.setNumero(apuesta.getNumero());
				historicoApuestas.setComision(apuesta.getComision());
				historicoApuestas.setCambio(apuesta.getCambio());
				historicoApuestas.setDate(apuesta.getDate());
				historicoApuestas.setMoneda(jugador.getMoneda().getMonedaName().toString());
				double cantidadMultiplier = MathUtil.getCantidadMultiplier(jugador, apuesta, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName(), jugador.getMoneda().getMonedaName()).doubleValue();
				historicoApuestas.setCantidadMultiplier(cantidadMultiplier);
				double premioMultiplier = MathUtil.getPremioMultiplier(jugador, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName()).doubleValue();
				historicoApuestas.setPremioMultiplier(premioMultiplier);
				historicoApuestaRepository.save(historicoApuestas);
				apuestaRepository.delete(apuesta);
			});
		} catch (Exception e) {
			throw e;
		} finally {
			logger.debug("copyApuestasToHistoricoApuestas(SorteoDiaria sorteoDiaria): END");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	private void deleteAndCreateSorteoDiaria(SorteoDiaria sorteoDiaria) {
		try {
			logger.debug("deleteAndCreateSorteoDiaria(SorteoDiaria {}): START", sorteoDiaria);
			sorteoDiariaRepository.delete(sorteoDiaria);
			logger.debug("sorteoDiariaRepository.delete({});", sorteoDiaria);

			int dayNextSorteo = Util.isSorteoTypeDiaria(sorteoDiaria.getSorteo()) ? 1 : 7;
			Timestamp timestamp;
			LocalDateTime horaSorteoNuevo = sorteoDiaria.getSorteo().getSorteoTime().toLocalDateTime()
					.plusDays(dayNextSorteo);
			timestamp = Timestamp.valueOf(horaSorteoNuevo);

			Sorteo sorteo = new Sorteo();
			sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.ABIERTA));
			sorteo.setSorteoTime(timestamp);
			sorteo.setSorteoType(sorteoDiaria.getSorteo().getSorteoType());
			sorteo.setSorteoType(sorteoTypeRepository
					.getBySorteoTypeName(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName()));
			sorteoRepository.save(sorteo);

			logger.debug("sorteoRepository.save({});", sorteo);

			SorteoDiaria newSorteoDiaria = new SorteoDiaria();
			newSorteoDiaria.setId(sorteo.getId());
			newSorteoDiaria.setSorteo(sorteo);
			newSorteoDiaria.setSorteoTime(timestamp);
			sorteoDiariaRepository.save(newSorteoDiaria);
			logger.debug("sorteoDiariaRepository.save({});", newSorteoDiaria);
		} catch (Exception e) {
			logger.error("deleteAndCreateSorteoDiaria(SorteoDiaria {}): CATCH", sorteoDiaria);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("deleteAndCreateSorteoDiaria(SorteoDiaria sorteoDiaria): END");
		}
	}

	@Override
	@Transactional(rollbackFor = InvalidSorteoStateException.class)
	public Sorteo bloquearApuesta(Long id) throws InvalidSorteoStateException {
		Sorteo sorteo = sorteoRepository.getSorteoById(id);
		try {
			logger.debug("bloquearApuesta(Long {}): START", id);
			if (sorteo.getEstado().getEstado().equals(EstadoName.ABIERTA)) {
				sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.BLOQUEADA));
				sorteoRepository.save(sorteo);
				historyService.createEvent(HistoryEventType.SORTEO_LOCKED);
			} else {
				logger.debug("InvalidSorteoStateException({}):", sorteo);
				throw new InvalidSorteoStateException(sorteo);
			}
		} catch (Exception e) {
			logger.error("bloquearApuesta(Long {}): CATCH", id);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("bloquearApuesta(Long id): END");
		}
		return sorteo;
	}

	@Override
	@Transactional(rollbackFor = { InvalidSorteoStateException.class, Exception.class })
	public Sorteo desBloquearApuesta(Long id) throws InvalidSorteoStateException {
		Sorteo sorteo = sorteoRepository.getSorteoById(id);
		try {
			logger.debug("desBloquearApuesta(Long {}): START", id);
			if (sorteo.getEstado().getEstado().equals(EstadoName.BLOQUEADA)) {
				sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.ABIERTA));
				sorteoRepository.save(sorteo);
				historyService.createEvent(HistoryEventType.SORTEO_LOCKED);
			} else {
				throw new InvalidSorteoStateException(sorteo);
			}
		} catch (Exception e) {
			logger.error("desBloquearApuesta(Long {}): CATCH", id);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("desBloquearApuesta(Long id): END");
		}
		return sorteo;
	}

	@Override
	public Sorteo forceCloseStatus(Long id) {
		Sorteo sorteo = sorteoRepository.getSorteoById(id);
		try {
			logger.debug("forceCloseStatus(Long {}): START", id);
			sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.CERRADA));
			sorteoRepository.save(sorteo);
			historyService.createEvent(HistoryEventType.SORTEO_LOCKED);
		} catch (Exception e) {
			logger.error("forceCloseStatus(Long {}): CATCH", id);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("forceCloseStatus(Long id): END");
		}
		return sorteo;
	}

	@Override
	public ApuestaActivaResumenResponse getDetalleApuestasBySorteo(Long id, String monedaType) {
		ApuestaActivaResumenResponse result = null;
		try {
			logger.debug("getDetalleApuestasBySorteo(Long {}, String {}): START", id, monedaType);
			SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
			sorteoTotales.processSorteo(null, sorteoDiaria, Util.getMonedaNameFromString(monedaType), true);

			int indexTopRiesgo = -1;
			double topRiesgo = 0d;

			BigDecimal totalValue = BigDecimal.ZERO;
			BigDecimal totalComision = BigDecimal.ZERO;
			Map<Integer, TuplaRiesgo> tuplas = new HashMap<>();
			for (Apuesta apuesta : sorteoDiaria.getApuestas()) {
				int numero = apuesta.getNumero();
				Jugador jugador = Util.getJugadorFromApuesta(apuesta);

				BigDecimal cambio = Util.getApuestaCambio(monedaType, apuesta);
				BigDecimal costoUnidad = MathUtil.getCantidadMultiplier(jugador, apuesta,sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName(), sorteoTotales.getMonedaName());
				BigDecimal cantidadTotal = BigDecimal.valueOf(apuesta.getCantidad()).multiply(cambio)
						.multiply(costoUnidad);

				BigDecimal comisionTotal = apuesta.getComision().doubleValue() == 0
						? BigDecimal.valueOf(apuesta.getComision())
						: BigDecimal.valueOf(apuesta.getComision()).multiply(cambio);

				BigDecimal premio = getPremioFromApuesta(jugador, apuesta,
						sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
				premio = premio.multiply(cambio);

				TuplaRiesgo tupla = tuplas.get(numero);
				if (tupla == null) {
					tupla = new TuplaRiesgo();
					tupla.setNumero(numero);
				}

				BigDecimal dineroApostado = BigDecimal.valueOf(tupla.getDineroApostado()).add(cantidadTotal);
				tupla.setDineroApostado(dineroApostado.doubleValue());

				BigDecimal totalPremio = BigDecimal.valueOf(tupla.getPosiblePremio()).add(premio);
				tupla.setPosiblePremio(totalPremio.doubleValue());
				if (topRiesgo < totalPremio.doubleValue()) {
					indexTopRiesgo = numero;
				}

				BigDecimal total = cantidadTotal.subtract(comisionTotal);
				BigDecimal riesgo = premio.divide(sorteoTotales.getTotalBD(), 2, RoundingMode.HALF_EVEN);
				// riesgo = BigDecimal.valueOf(tupla.getTotalRiesgo()).add(riesgo);
				tupla.setTotalRiesgo(BigDecimal.valueOf(tupla.getTotalRiesgo()).add(riesgo).doubleValue());

				tuplas.put(numero, tupla);
				totalComision = totalComision.add(comisionTotal);
				totalValue = totalValue.add(cantidadTotal);
			}

			TuplaRiesgo tuplaRiesgo = new TuplaRiesgo();
			if (indexTopRiesgo != -1) {
				tuplaRiesgo = tuplas.get(indexTopRiesgo);
			}

			result = new ApuestaActivaResumenResponse(tuplaRiesgo, new ArrayList<TuplaRiesgo>(tuplas.values()),
					totalComision.doubleValue(), totalValue.doubleValue());
		} catch (Exception e) {
			logger.error("getDetalleApuestasBySorteo(Long {}, String {}): CATCH", id, monedaType);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("getDetalleApuestasBySorteo(Long id, String monedaType): END");
		}

		return result;
	}

	private BigDecimal getPremioFromApuesta(Jugador jugador, Apuesta apuesta, SorteoTypeName sorteoType) {
		BigDecimal premio = BigDecimal.ZERO;
		try {
			if (sorteoType.equals(SorteoTypeName.DIARIA)) {
				if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.DIRECTO)) {
					premio = BigDecimal.valueOf(jugador.getPremioDirecto());
				} else if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.MILES)) {
					premio = BigDecimal.valueOf(jugador.getPremioMil());
				}
			} else if (sorteoType.equals(SorteoTypeName.CHICA)) {
				if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
					premio = BigDecimal.valueOf(jugador.getPremioChicaDirecto());
				} else if (jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)) {
					premio = BigDecimal.valueOf(jugador.getPremioChicaMiles());
				} else if (jugador.getTipoChica().getChicaName().equals(ChicaName.PEDAZOS)) {
					premio = BigDecimal.valueOf(jugador.getPremioChicaPedazos());
				}
			}
		} catch (Exception e) {
			logger.error("getPremioFromApuesta(Jugador {}, Apuesta {}, SorteoTypeName {}): CATCH", jugador, apuesta,
					sorteoType);
			logger.error(e.getMessage());
			throw e;
		}
		return premio.multiply(BigDecimal.valueOf(apuesta.getCantidad()));
	}

	@Override
	@Transactional(rollbackFor = { CanNotInsertApuestaException.class, SorteoEstadoNotValidException.class,
			Exception.class })
	public void submitApuestas(String username, Long sorteoId, List<NumeroPlayerEntryResponse> apuestasEntry)
			throws CanNotInsertApuestaException, SorteoEstadoNotValidException {

		try {
			logger.debug("submitApuestas(String {}, Long {}, List<NumeroPlayerEntryResponse> {}): START", username,
					sorteoId, apuestasEntry);
			User user = userRepository.getByUsername(username);
			Jugador jugador = Util.getJugadorFromUser(user);
			Cambio cambio = cambioRepository.findFirstByOrderByIdDesc();
			SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(sorteoId);

			if (!sorteoDiaria.getSorteo().getEstado().getEstado().equals(EstadoName.ABIERTA)) {
				throw new SorteoEstadoNotValidException("El sorteo debe de estar abierto para poder comprar apuestas");
			}

			Set<Apuesta> apuestasExistentes = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);

			for (NumeroPlayerEntryResponse entryResponse : apuestasEntry) {
				try {
					Apuesta apuesta = apuestasExistentes.stream()
							.filter(a -> a.getNumero() == Integer.parseInt(entryResponse.getNumero())).findFirst()
							.orElse(null);

					if (apuesta == null) {
						apuesta = new Apuesta();
						apuesta.setNumero(Integer.parseInt(entryResponse.getNumero()));
						apuesta.setUser(user);
						apuesta.setSorteoDiaria(sorteoDiaria);

						apuesta.setCantidad(Double.valueOf(0d));
					}
					apuesta.setCantidad(apuesta.getCantidad() + entryResponse.getCurrent());
					apuesta.setCambio(cambio);

					BigDecimal comisionRate = MathUtil.getComisionRate(jugador, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
					comisionRate = comisionRate.divide(BigDecimal.valueOf(100));
					BigDecimal comision = comisionRate.multiply(BigDecimal.valueOf(apuesta.getCantidad()));
					apuesta.setComision(comision.doubleValue());
					apuesta.setDate(Timestamp.valueOf(LocalDateTime.now()));
					apuestaRepository.save(apuesta);
				} catch (Exception e) {
					e.printStackTrace();
					throw new CanNotInsertApuestaException(sorteoDiaria.getSorteo().getSorteoTime().toString(),
							entryResponse.getNumero(), e.getMessage());
				}
			}
		} catch (CanNotInsertApuestaException ex) {
			logger.error("submitApuestas(String {}, Long {}, List<NumeroPlayerEntryResponse> {}): CATCH", username,
					sorteoId, apuestasEntry);
			logger.error(ex.getMessage());
			throw ex;
		} finally {
			logger.debug(
					"submitApuestas(String username, Long sorteoId, List<NumeroPlayerEntryResponse> apuestasEntry): END");
		}

	}

	public ApuestaActivaResponse getApuestasActivasBySorteoAndJugador(Long sorteoId, String username) {

		ApuestaActivaResponse apuestaActivaResponse = null;
		try {
			logger.debug("getApuestasActivasBySorteoAndJugador(Long {}, String {}): START", sorteoId, username);
			User user = userRepository.getByUsername(username);
			Jugador jugador = Util.getJugadorFromUser(user);
			SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(sorteoId);
			List<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUserOrderByNumeroAsc(sorteoDiaria, user);

			sorteoTotales.processSorteo(user, sorteoDiaria);
			List<PairNV> pairNVList = new ArrayList<>();
			mergeApuestasIntoPairNVList(pairNVList, apuestas);

			if (user instanceof Jugador) {
				List<Asistente> asistentes = asistenteRepository.findAllByJugador(jugador);
				asistentes.forEach(asistente -> {
					List<Apuesta> asistenteApuestasList = apuestaRepository
							.findAllBySorteoDiariaAndUserOrderByNumeroAsc(sorteoDiaria, asistente);
					if (!asistenteApuestasList.isEmpty()) {
						mergeApuestasIntoPairNVList(pairNVList, asistenteApuestasList);
					}
				});
			}

			Collections.sort(pairNVList);
			apuestaActivaResponse = new ApuestaActivaResponse();
			apuestaActivaResponse.setList(pairNVList);
			apuestaActivaResponse.setTitle(Util.formatTimestamp2String(sorteoDiaria.getSorteoTime()));
			apuestaActivaResponse.setDay(Util.getDayFromTimestamp(sorteoDiaria.getSorteoTime()));
			apuestaActivaResponse.setHour(Util.getHourFromTimestamp(sorteoDiaria.getSorteoTime()));
			apuestaActivaResponse.setTotal(sorteoTotales.getVentas());
			apuestaActivaResponse.setComision(sorteoTotales.getComisiones());
			apuestaActivaResponse.setRiesgo(sorteoTotales.getTotal());
			apuestaActivaResponse.setType(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString());

		} catch (Exception e) {
			logger.error("getApuestasActivasBySorteoAndJugador(Long {}, String {}): CATCH", sorteoId, username);
			logger.error(e.getMessage());
			throw e;
		} finally {
			logger.debug("getApuestasActivasBySorteoAndJugador(Long sorteoId, String username): END");
		}
		return apuestaActivaResponse;

	}

	private void mergeApuestasIntoPairNVList(List<PairNV> pairNVList, List<Apuesta> apuestas) {
		for (Apuesta apuesta : apuestas) {
			PairNV jugadorPair = pairNVList.stream()
					.filter(i -> i.getNumero().intValue() == apuesta.getNumero().intValue()).findFirst().orElse(null);
			if (jugadorPair == null) {
				jugadorPair = new PairNV(apuesta.getNumero(), apuesta.getCantidad());
				pairNVList.add(jugadorPair);
			} else {
				jugadorPair.setValor(jugadorPair.getValor() + apuesta.getCantidad());
			}
		}
	}

	@Override
	public void deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long sorteoId, Integer numero, String username)
			throws CanNotRemoveApuestaException, SorteoEstadoNotValidException {
		SorteoDiaria sorteoDiaria = null;
		try {
			logger.debug("deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long {}, Integer {}, String {}): START",
					sorteoId, numero, username);
			User user = userRepository.getByUsername(username);
			sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(sorteoId);

			if (!sorteoDiaria.getSorteo().getEstado().getEstado().equals(EstadoName.ABIERTA)
					&& !sorteoDiaria.getSorteo().getEstado().getEstado().equals(EstadoName.BLOQUEADA)) {
				throw new SorteoEstadoNotValidException("No se puede eliminar apuestas de un sorteo cerrado");
			}

			List<Apuesta> apuestasP = apuestaRepository.findAllBySorteoDiariaAndNumeroAndUser(sorteoDiaria, numero,
					user);
			deleteApuestas(apuestasP);

			if (user instanceof Jugador) {
				List<Asistente> asistentes = asistenteRepository.findAllByJugador((Jugador) user);
				for (Asistente asistente : asistentes) {
					List<Apuesta> apuestasX = apuestaRepository.findAllBySorteoDiariaAndNumeroAndUser(sorteoDiaria,
							numero, asistente);
					deleteApuestas(apuestasX);
				}
			}
			historyService.createEvent(HistoryEventType.BET_DELETED,sorteoId,String.valueOf(numero),null);
		} catch (Exception e) {
			logger.error("deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long {}, Integer {}, String {}): CATCH",
					sorteoId, numero, username);
			logger.error(e.getMessage());

			if (e instanceof SorteoEstadoNotValidException) {
				throw new SorteoEstadoNotValidException(e.getMessage());
			} else {
				throw new CanNotRemoveApuestaException(
						sorteoDiaria != null ? sorteoDiaria.getSorteo().getSorteoTime().toString()
								: "Sorteo Id" + sorteoId,
						numero.toString(), e.getMessage());
			}
		} finally {
			logger.debug("deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long sorteoId, Integer numero, String username): END");
		}
	}

	private void deleteApuestas(List<Apuesta> apuestas) {
		for (Apuesta apuesta : apuestas) {
			apuestaRepository.delete(apuesta);
		}
	}

	@Override
	public void deleteAllApuestasOnSorteoDiarioByUser(Long sorteoId, String username)
			throws CanNotRemoveApuestaException, SorteoEstadoNotValidException {
		SorteoDiaria sorteoDiaria = null;
		List<Integer> numeros = new ArrayList<>();
		
		try {
			logger.debug("deleteAllApuestasOnSorteoDiarioByUser(Long {}, String {}): END", sorteoId, username);
			User user = userRepository.getByUsername(username);
			sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(sorteoId);

			if (!sorteoDiaria.getSorteo().getEstado().getEstado().equals(EstadoName.ABIERTA)
					&& !sorteoDiaria.getSorteo().getEstado().getEstado().equals(EstadoName.BLOQUEADA)) {
				throw new SorteoEstadoNotValidException("No se puede eliminar apuestas de un sorteo cerrado");
			}

			Set<Apuesta> apuestasP = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
			apuestasP.forEach(x ->{numeros.add(x.getNumero());});
			deleteApuestas(apuestasP.stream().collect(Collectors.toList()));

			if (user instanceof Jugador) {
				List<Asistente> asistentes = asistenteRepository.findAllByJugador((Jugador) user);
				for (Asistente asistente : asistentes) {
					Set<Apuesta> apuestasX = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
					apuestasX.forEach(x ->{numeros.add(x.getNumero());});
					deleteApuestas(apuestasX.stream().collect(Collectors.toList()));
				}
			}
			historyService.createEvent(HistoryEventType.BET_DELETED,sorteoId,numeros.toArray().toString(),null);
		} catch (Exception e) {
			logger.error("deleteAllApuestasOnSorteoDiarioByUser(Long {}, String {}): CATCH", sorteoId, username);
			logger.error(e.getMessage());

			if (e instanceof SorteoEstadoNotValidException) {
				throw new SorteoEstadoNotValidException(e.getMessage());
			} else {
				throw new CanNotRemoveApuestaException(
						sorteoDiaria != null ? sorteoDiaria.getSorteo().getSorteoTime().toString()
								: "Sorteo Id" + sorteoId,
						e.getMessage());
			}
		} finally {
			logger.debug("deleteAllApuestasOnSorteoDiarioByUser(Long sorteoId, String username): END");
		}
	}

	private Week getWeekFromSorteoTime(Timestamp sorteoTime) {
		LocalDateTime mondayFirstSorteo = sorteoTime.toLocalDateTime();
		mondayFirstSorteo = mondayFirstSorteo.with(DayOfWeek.MONDAY);
		mondayFirstSorteo = mondayFirstSorteo.with(LocalTime.of(11, 0, 0));

		LocalDateTime sundayLastSorteo = sorteoTime.toLocalDateTime();
		sundayLastSorteo = sundayLastSorteo.with(DayOfWeek.SUNDAY);
		sundayLastSorteo = sundayLastSorteo.with(LocalTime.of(21, 0, 0));
		
		Week week = weekRepository.findByMondayAndSunday(Timestamp.valueOf(mondayFirstSorteo), Timestamp.valueOf(sundayLastSorteo));
		if( week == null) {
			week = new Week();
			week.setYear(mondayFirstSorteo.getYear());
			week.setMonday(Timestamp.valueOf(mondayFirstSorteo));
			week.setSunday(Timestamp.valueOf(sundayLastSorteo));
			week = weekRepository.save(week);
		}
		return week;
	}
}
