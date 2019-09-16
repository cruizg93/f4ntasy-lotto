package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.math.SorteoTotales;
import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorSorteosResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
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
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.SorteoTypeName;
import com.devteam.fantasy.util.TuplaRiesgo;
import com.devteam.fantasy.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

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
}



















