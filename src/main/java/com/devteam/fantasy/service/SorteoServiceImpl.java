package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.exception.ApuestaNotFoundException;
import com.devteam.fantasy.exception.CanNotInsertApuestaException;
import com.devteam.fantasy.exception.InvalidSorteoStateException;
import com.devteam.fantasy.math.SorteoTotales;
import com.devteam.fantasy.message.response.ApuestaActivaResponse;
import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorSorteosResponse;
import com.devteam.fantasy.message.response.NumeroPlayerEntryResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Cambio;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.repository.ApuestaRepository;
import com.devteam.fantasy.repository.AsistenteRepository;
import com.devteam.fantasy.repository.CambioRepository;
import com.devteam.fantasy.repository.EstadoRepository;
import com.devteam.fantasy.repository.HistoricoApuestaRepository;
import com.devteam.fantasy.repository.JugadorRepository;
import com.devteam.fantasy.repository.NumeroGanadorRepository;
import com.devteam.fantasy.repository.RestriccionRepository;
import com.devteam.fantasy.repository.ResultadoRepository;
import com.devteam.fantasy.repository.SorteoDiariaRepository;
import com.devteam.fantasy.repository.SorteoRepository;
import com.devteam.fantasy.repository.UserRepository;
import com.devteam.fantasy.util.ApostadorName;
import com.devteam.fantasy.util.ChicaName;
import com.devteam.fantasy.util.EstadoName;
import com.devteam.fantasy.util.HistoryEventType;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.PairNV;
import com.devteam.fantasy.util.SorteoTypeName;
import com.devteam.fantasy.util.TuplaRiesgo;
import com.devteam.fantasy.util.Util;

@Service
public class SorteoServiceImpl implements SorteoService{

	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	SorteoRepository sorteoRepository;
	
	@Autowired
	SorteoDiariaRepository sorteoDiariaRepository;
	
	@Autowired
	RestriccionRepository restriccionRepository;
	
	@Autowired
	ApuestaRepository apuestaRepository;
	
	@Autowired
	EstadoRepository estadoRepository;
	
	@Autowired
	AsistenteRepository asistenteRepository;
	
	@Autowired
	JugadorRepository jugadorRepository;
	
	@Autowired
	CambioRepository cambioRepository;
	
	@Autowired
	HistoricoApuestaRepository historicoApuestaRepository;
	
	@Autowired
	ResultadoRepository resultadoRepository;
	
	@Autowired
	NumeroGanadorRepository numeroGanadorRepository;
	
	@Autowired
	SorteoTotales sorteoTotales;
	
	@Autowired
	HistoryService historyService;
	
	/**
	 * Type [Diaria] [Sorteos] must be sort by time, 
	 * but keeping the original position of [Sorteo] [Chica]    
	 */
	public List<SorteoDiaria> getActiveSorteosList() {
		return getActiveSorteosList(null);
	}
	public List<SorteoDiaria> getActiveSorteosList(User user) {
		Iterable<SorteoDiaria> sorteosDB = sorteoDiariaRepository.findAll();
		List<SorteoDiaria> sorteos = sortDiariaList(sorteosDB);
		
        for (SorteoDiaria sorteoDiaria :  sorteos) {
        	Set<Apuesta> apuestas = null;
        	if(user == null) {
        		apuestas = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);
        	}else {
        		apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
        	}
            sorteoDiaria.setApuestas(apuestas);
        }
        return sorteos;
	}
	
	public ApuestaActivaResumenResponse getActiveSorteoDetail(Long id, String currency) {
		List<TuplaRiesgo> tuplaRiesgos = new ArrayList<>();
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        Sorteo sorteo = sorteoDiaria.getSorteo();
        Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);
        
        double[] cantidad = new double[100];
        double[] riesgo = new double[100];
        final double[] total = {0.0};
        final double[] comision = {0.0};
        
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

        return new ApuestaActivaResumenResponse(tuplaRiesgo, tuplaRiesgos, comision[0], totalValue);
	}
	
	private List<SorteoDiaria> sortDiariaList(Iterable<SorteoDiaria> list){
		List<SorteoDiaria> sorteos = new LinkedList<SorteoDiaria>();
		list.forEach(sorteos::add);

		int indexChica = IntStream.range(0, sorteos.size())
			     .filter(i -> SorteoTypeName.CHICA.equals(sorteos.get(i).getSorteo().getSorteoType().getSorteoTypeName()))
			     .findFirst().getAsInt();
		
		SorteoDiaria sorteoChica = sorteos.get(indexChica);
		
		sorteos.remove(indexChica);
		sorteos.sort((sorteo1, sorteo2) -> sorteo1.getSorteoTime().compareTo(sorteo2.getSorteoTime()));
		sorteos.add(indexChica,sorteoChica);
		
		return sorteos;
	}
	
	/**
	 * Make sure to only pass the "sorteos" for the passed user,
	 * this meaning the list of sorteos should not include the asistente sorteos.
	 * ex. sorteos = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
	 */
	public List<SorteoResponse> getSorteosResponses(List<SorteoDiaria> sorteos, User user) {
        List<SorteoResponse> sorteoResponses = new ArrayList<>();
        Jugador jugador = Util.getJugadorFromUser(user);
        MonedaName moneda = jugador.getMoneda().getMonedaName();
        
        for (SorteoDiaria sorteoDiaria : sorteos) {
        	sorteoTotales.processSorteo(jugador, sorteoDiaria);
            
        	String estado = sorteoDiaria.getSorteo().getEstado().getEstado().toString();
            sorteoResponses.add(new SorteoResponse(sorteoDiaria.getId(),
                    Util.formatTimestamp2String(sorteoDiaria.getSorteoTime()),
                    Util.getDayFromTimestamp(sorteoDiaria.getSorteoTime()),
                    Util.getHourFromTimestamp(sorteoDiaria.getSorteoTime()),
                    sorteoTotales.getVentas(), sorteoTotales.getComisiones(), sorteoTotales.getTotal(), 
                    estado, moneda.toString(), sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString()));
        }
        
        return sorteoResponses;
	}
	
	private void calcularCantRiesgo(Sorteo sorteo, double[] cantidad, double[] riesgo, Apuesta apuesta, int numero,
			Jugador jugador, double[] total, String currency, double[] comision) {

		BigDecimal cambio = Util.getApuestaCambio(currency, apuesta);
		BigDecimal premio = BigDecimal.ZERO;
		
		BigDecimal costoMilChica =  BigDecimal.ONE;
		BigDecimal costoMilDiaria = BigDecimal.ONE;
		BigDecimal costoPedazoChica = BigDecimal.ONE;
		
		if (Util.isSorteoTypeDiaria(sorteo)) {
			if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.DIRECTO)) {
				premio = BigDecimal.valueOf(jugador.getPremioDirecto())
							.multiply(BigDecimal.valueOf(apuesta.getCantidad()))
							.multiply(cambio);
			} else {
				premio = BigDecimal.valueOf(jugador.getPremioMil())
								.multiply(BigDecimal.valueOf(apuesta.getCantidad()))
								.multiply(cambio);
			}
			
			costoMilDiaria = jugador.getCostoMil() != 0 ? BigDecimal.valueOf(jugador.getCostoMil()) : BigDecimal.ONE;

			cantidad[numero] += BigDecimal.valueOf(apuesta.getCantidad()).multiply(cambio).multiply(costoMilDiaria).doubleValue();
			comision[0] += cambio.multiply(BigDecimal.valueOf(apuesta.getComision())).doubleValue();
			riesgo[numero] += premio.doubleValue();
			
		} else {
			if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
				premio = BigDecimal.valueOf(jugador.getPremioChicaDirecto())
						.multiply(BigDecimal.valueOf(apuesta.getCantidad()))
						.multiply(cambio);
				
			} else if (jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)) {
				premio = BigDecimal.valueOf(jugador.getPremioChicaMiles())
						.multiply(BigDecimal.valueOf(1000)
						.multiply(BigDecimal.valueOf(apuesta.getCantidad())))
						.multiply(cambio);
				
			} else {
				premio = BigDecimal.valueOf(jugador.getPremioChicaPedazos())
						.multiply(BigDecimal.valueOf(apuesta.getCantidad()))
						.multiply(cambio);
			}
			
			costoPedazoChica = jugador.getCostoChicaPedazos() != 0 ? BigDecimal.valueOf(jugador.getCostoChicaPedazos()) : BigDecimal.ONE;
			costoMilChica = jugador.getCostoChicaMiles() != 0 ? BigDecimal.valueOf(jugador.getCostoChicaMiles()) : BigDecimal.ONE;

			cantidad[numero] += BigDecimal.valueOf(apuesta.getCantidad())
						.multiply(cambio)
						.multiply(costoMilChica)
						.multiply(costoPedazoChica)
						.doubleValue();
			
			comision[0] += BigDecimal.valueOf(apuesta.getComision()).multiply(cambio).doubleValue();
			riesgo[numero] += premio.doubleValue();
		}
		total[0] += premio.doubleValue();
	}
	
	@Override
	public List<ApuestasActivasResponse> getSorteosListWithMoneda(String currency) {
		List<SorteoDiaria> sorteoDiarias = getActiveSorteosList();
		List<ApuestasActivasResponse> apuestasActivasResponses = new ArrayList<>();
		
		sorteoDiarias.forEach(sorteoDiaria -> {
			apuestasActivasResponses.add(getApuestasActivasResponse(sorteoDiaria, currency));
		});
		return apuestasActivasResponses;
	}
	
	private ApuestasActivasResponse getApuestasActivasResponse( SorteoDiaria sorteoDiaria, String currency) {
		
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal comision = BigDecimal.ZERO;
		BigDecimal premio = BigDecimal.ZERO;
		BigDecimal neta = BigDecimal.ZERO;
		
		MonedaName moneda = currency.equalsIgnoreCase(MonedaName.LEMPIRA.toString())?MonedaName.LEMPIRA:MonedaName.DOLAR;

        sorteoTotales.processSorteo(null, sorteoDiaria,moneda, true);
        total = total.add(sorteoTotales.getVentasBD());
        comision = comision.add(sorteoTotales.getComisionesBD());
        
        neta = total.subtract(comision);
        ApuestasActivasResponse activaResponse = new ApuestasActivasResponse();
        activaResponse.setTotal(total.doubleValue());
        activaResponse.setComision(comision.doubleValue());
        activaResponse.setNeta(neta.doubleValue());
        activaResponse.setPremio(premio.doubleValue());
        activaResponse.setBalance(neta.subtract(premio).doubleValue());
        activaResponse.setId(sorteoDiaria.getSorteo().getId());
        activaResponse.setTitle(Util.formatTimestamp2String(sorteoDiaria.getSorteo().getSorteoTime()));
        activaResponse.setEstado(sorteoDiaria.getSorteo().getEstado().getEstado().toString());
        activaResponse.setType(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString());
		return activaResponse;
	}

	public JugadorSorteosResponse getJugadorList() {
		JugadorSorteosResponse jugadorSorteosResponse = new JugadorSorteosResponse();
        User user = userService.getLoggedInUser();
        Jugador jugador = Util.getJugadorFromUser(user);
        jugadorSorteosResponse.setName(user.getName());
        jugadorSorteosResponse.setMoneda(jugador.getMoneda().getMonedaName().toString());
        
        Iterable<SorteoDiaria> sorteosDB = sorteoDiariaRepository.findAll();
        List<SorteoDiaria> sorteos = sortDiariaList(sorteosDB);       
        jugadorSorteosResponse.setSorteos(getSorteosResponses(sorteos, user));
        		
        return jugadorSorteosResponse;
		
	}
	
	@Override
	public void setNumeroGanador(Long id, int numeroGanador) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Sorteo bloquearApuesta(Long id) throws InvalidSorteoStateException{
		Sorteo sorteo = sorteoRepository.getSorteoById(id);
		if(sorteo.getEstado().getEstado().equals(EstadoName.ABIERTA)) {
			sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.BLOQUEADA));
	        sorteoRepository.save(sorteo);
	        historyService.createEvent(HistoryEventType.BLOQUEDA, "Sorteo Bloqueado");
		}else {
			throw new InvalidSorteoStateException(sorteo);
		}
        return sorteo;
	}
	@Override
	public Sorteo desBloquearApuesta(Long id) throws InvalidSorteoStateException {
		Sorteo sorteo = sorteoRepository.getSorteoById(id);
		if(sorteo.getEstado().getEstado().equals(EstadoName.BLOQUEADA)) {
			sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.ABIERTA));
	        sorteoRepository.save(sorteo);
	        historyService.createEvent(HistoryEventType.BLOQUEDA, "Sorteo Desbloqueado");
		}else {
			throw new InvalidSorteoStateException(sorteo);
		}
        return sorteo;
	}
	@Override
	public Sorteo forceCloseStatus(Long id) {
		Sorteo sorteo = sorteoRepository.getSorteoById(id);
		sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.CERRADA));
        sorteoRepository.save(sorteo);
        historyService.createEvent(HistoryEventType.BLOQUEDA, "Close status forced.");
        return sorteo;
	}
	@Override
	public ApuestaActivaResumenResponse getDetalleApuestasBySorteo(Long id, String monedaType) {
		
		 List<TuplaRiesgo> tuplaRiesgos = new ArrayList<>();
		 SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
		 
		 int indexTopRiesgo = -1;
		 double topRiesgo = 0d;
		 
		 BigDecimal totalValue = BigDecimal.ZERO;
		 BigDecimal totalComision = BigDecimal.ZERO;
		 Map<Integer, TuplaRiesgo> tuplas = new HashMap<>();
		 for(Apuesta apuesta: sorteoDiaria.getApuestas()) {
			int numero = apuesta.getNumero();
			Jugador jugador = Util.getJugadorFromApuesta(apuesta);
			
			BigDecimal cambio = Util.getApuestaCambio(monedaType, apuesta);
			BigDecimal costoUnidad = sorteoTotales.getCantidadMultiplier(jugador, apuesta, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
			BigDecimal cantidadTotal = BigDecimal.valueOf(apuesta.getCantidad()).multiply(cambio).multiply(costoUnidad);
			
			BigDecimal comisionTotal = apuesta.getComision().doubleValue()==0
							?BigDecimal.valueOf(apuesta.getComision())
							:BigDecimal.valueOf(apuesta.getComision()).multiply(cambio);
			
			BigDecimal premio = getPremioFromApuesta(jugador, apuesta, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
			premio = premio.multiply(cambio);
			
			TuplaRiesgo tupla = tuplas.get(numero);
			if(tupla == null) {
				tupla = new TuplaRiesgo();
				tupla.setNumero(numero);
			}
			
			BigDecimal dineroApostado = BigDecimal.valueOf(tupla.getDineroApostado()).add(cantidadTotal);
			tupla.setDineroApostado(dineroApostado.doubleValue());
			
			BigDecimal totalRiesgo = BigDecimal.valueOf(tupla.getPosiblePremio()).add(premio);
			tupla.setPosiblePremio(totalRiesgo.doubleValue());
			if(topRiesgo < totalRiesgo.doubleValue()) {
				indexTopRiesgo = numero;
			}
			
			//premio / (venta - comision) 
			BigDecimal total = cantidadTotal.subtract(comisionTotal);
			BigDecimal riesgo = premio.divide(total, 2, RoundingMode.HALF_EVEN);
//			riesgo = BigDecimal.valueOf(tupla.getTotalRiesgo()).add(riesgo);
			tupla.setTotalRiesgo(riesgo.doubleValue());
			
			tuplas.put(numero, tupla);
			totalComision = totalComision.add(comisionTotal);
			totalValue = totalValue.add(cantidadTotal);
		 }
		
		 
		TuplaRiesgo tuplaRiesgo = new TuplaRiesgo();
		if (indexTopRiesgo != -1) {
			tuplaRiesgo = tuplas.get(indexTopRiesgo);
        }
		
		
		
		return new ApuestaActivaResumenResponse(tuplaRiesgo, new ArrayList<TuplaRiesgo>(tuplas.values()), totalComision.doubleValue(), totalValue.doubleValue());
	}
	
	
	private BigDecimal getPremioFromApuesta(Jugador jugador, Apuesta apuesta, SorteoTypeName sorteoType) {
		BigDecimal premio = BigDecimal.ZERO;
		
		if(sorteoType.equals(SorteoTypeName.DIARIA)) {
			if( jugador.getTipoApostador().getApostadorName().equals(ApostadorName.DIRECTO)) {
				premio = BigDecimal.valueOf(jugador.getPremioDirecto());
			}else if( jugador.getTipoApostador().getApostadorName().equals(ApostadorName.MILES)) {
				premio = BigDecimal.valueOf(jugador.getPremioMil());
			}
		} else if(sorteoType.equals(SorteoTypeName.CHICA)) {
            if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
            	premio = BigDecimal.valueOf(jugador.getPremioChicaDirecto());
            } else if (jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)) {
            	premio = BigDecimal.valueOf(jugador.getPremioChicaMiles());
            } else if (jugador.getTipoChica().getChicaName().equals(ChicaName.PEDAZOS)) {
            	premio = BigDecimal.valueOf(jugador.getPremioChicaPedazos());
            }
        }
		
		return premio.multiply(BigDecimal.valueOf(apuesta.getCantidad()));
	}
	@Override
	public void submitApuestas(String username, Long sorteoId, List<NumeroPlayerEntryResponse> apuestasEntry) throws CanNotInsertApuestaException {
		User user = userRepository.getByUsername(username);
        Jugador jugador = Util.getJugadorFromUser(user);
		Cambio cambio = cambioRepository.findFirstByOrderByIdDesc();
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(sorteoId);
        Set<Apuesta> apuestasExistentes = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
        
        for (NumeroPlayerEntryResponse entryResponse : apuestasEntry) {
        	try {
        		Apuesta apuesta = apuestasExistentes.stream().
            		    filter(a -> a.getNumero() == Integer.parseInt(entryResponse.getNumero()) ).
            		    findFirst().orElse(null);
            	
            	if  (apuesta == null) {
            		apuesta = new Apuesta();
            		apuesta.setNumero(Integer.parseInt(entryResponse.getNumero()));
            		apuesta.setUser(user);
            		apuesta.setSorteoDiaria(sorteoDiaria);
            		
            		apuesta.setCantidad(Double.valueOf(0d));
            	}
            	apuesta.setCantidad(apuesta.getCantidad()+entryResponse.getCurrent());
            	apuesta.setCambio(cambio);

            	BigDecimal comisionRate = sorteoTotales.getComisionRate(jugador, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName()).divide(BigDecimal.valueOf(100));
            	BigDecimal comision = comisionRate.multiply(BigDecimal.valueOf(apuesta.getCantidad()));
            	apuesta.setComision(comision.doubleValue());
            	
            	apuestaRepository.save(apuesta);
        	}catch(Exception e) {
        		e.printStackTrace();
        		throw new CanNotInsertApuestaException(sorteoDiaria.getSorteo().getSorteoTime().toString(),entryResponse.getNumero(), e.getMessage());
        	}
        }
        
	}
	
	public ApuestaActivaResponse getApuestasActivasBySorteoAndJugador(Long sorteoId, String username) {
		
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
                List<Apuesta> asistenteApuestasList = apuestaRepository.findAllBySorteoDiariaAndUserOrderByNumeroAsc(sorteoDiaria, asistente);
                if (!asistenteApuestasList.isEmpty()) {
                	mergeApuestasIntoPairNVList(pairNVList, asistenteApuestasList);
                }
            });
		}
		
		Collections.sort(pairNVList);
        ApuestaActivaResponse apuestaActivaResponse = new ApuestaActivaResponse();
        apuestaActivaResponse.setList(pairNVList);
        apuestaActivaResponse.setTitle(Util.formatTimestamp2String(sorteoDiaria.getSorteoTime()));
        apuestaActivaResponse.setDay(Util.getDayFromTimestamp(sorteoDiaria.getSorteoTime()));
        apuestaActivaResponse.setHour(Util.getHourFromTimestamp(sorteoDiaria.getSorteoTime()));
        apuestaActivaResponse.setTotal(sorteoTotales.getVentas());
        apuestaActivaResponse.setComision(sorteoTotales.getComisiones());
        apuestaActivaResponse.setRiesgo(sorteoTotales.getTotal());
        apuestaActivaResponse.setType(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString());
        return apuestaActivaResponse;
	}
	
	private void mergeApuestasIntoPairNVList(List<PairNV> pairNVList, List<Apuesta> apuestas) {
		for (Apuesta apuesta : apuestas) {
			PairNV jugadorPair = pairNVList.stream().filter(i -> i.getNumero().intValue()==apuesta.getNumero().intValue()).findFirst().orElse(null);
        	if(jugadorPair == null) {
        		jugadorPair = new PairNV(apuesta.getNumero(), apuesta.getCantidad());
        		pairNVList.add(jugadorPair);
        	}else {
        		jugadorPair.setValor(jugadorPair.getValor() + apuesta.getCantidad());
        	}
		}
	}
	
	
	@Override
	public void deleteAllApuestasOnSorteoDiarioByNumeroAndUser(Long sorteoId, Integer numero, String username) {
		User user = userRepository.getByUsername(username);
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(sorteoId);
//        Apuesta selectedApuesta = apuestaRepository.findById(apuestaId).orElseThrow(() -> new ApuestaNotFoundException("No se encontro apuesta con id = "+apuestaId));

        List<Apuesta> apuestasP = apuestaRepository.findAllBySorteoDiariaAndNumeroAndUser(sorteoDiaria,numero, user);
        for(Apuesta apuestaP: apuestasP) {
        	apuestaRepository.delete(apuestaP);
        }
        if (user instanceof Jugador) {
            List<Asistente> asistentes = asistenteRepository.findAllByJugador((Jugador)user);
            for(Asistente asistente: asistentes) {
            	List<Apuesta> apuestasX = apuestaRepository.findAllBySorteoDiariaAndNumeroAndUser(sorteoDiaria,numero, asistente);
                for(Apuesta apuestaX: apuestasX) {
                	apuestaRepository.delete(apuestaX);
                }
            }
		}
	}
}



















