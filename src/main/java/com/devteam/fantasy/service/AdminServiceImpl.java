package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.exception.CanNotInsertBonoException;
import com.devteam.fantasy.message.request.BonoRequest;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.AsistenteResponse;
import com.devteam.fantasy.message.response.JugadorResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Bono;
import com.devteam.fantasy.model.Cambio;
import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Moneda;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.repository.ApuestaRepository;
import com.devteam.fantasy.repository.AsistenteRepository;
import com.devteam.fantasy.repository.BonoRepository;
import com.devteam.fantasy.repository.CambioRepository;
import com.devteam.fantasy.repository.JugadorRepository;
import com.devteam.fantasy.repository.MonedaRepository;
import com.devteam.fantasy.repository.NumeroGanadorRepository;
import com.devteam.fantasy.repository.ResultadoRepository;
import com.devteam.fantasy.repository.SorteoDiariaRepository;
import com.devteam.fantasy.repository.SorteoRepository;
import com.devteam.fantasy.repository.WeekRepository;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.SorteoTypeName;
import com.devteam.fantasy.util.Util;

import javassist.NotFoundException;

@Service
public class AdminServiceImpl implements AdminService{

	@Autowired
	private SorteoService sorteoService;

	@Autowired
	private UserService userService;
	
	@Autowired
    SorteoRepository sorteoRepository;
	
	@Autowired
    JugadorRepository jugadorRepository;
	
	@Autowired
    AsistenteRepository asistenteRepository;
	
	@Autowired
    SorteoDiariaRepository sorteoDiariaRepository;
	
	@Autowired
	MonedaRepository monedaRepository;
	
	@Autowired
    NumeroGanadorRepository numeroGanadorRepository;
	
	@Autowired	
    ResultadoRepository resultadoRepository;
	
	@Autowired
    ApuestaRepository apuestaRepository;
	
	@Autowired
	WeekRepository weekRepository;
	
	@Autowired
	BonoRepository bonoRepository;

    @Autowired
    private HistoryService historyService;
    
    @Autowired
    private CambioRepository cambioRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
	
	public List<JugadorResponse> getAllJugadores() throws Exception{
		List<JugadorResponse> jugadorResponses = new ArrayList<>();

		try {
			logger.debug("getAllJugadores(): START");
			List<Jugador> jugadores = jugadorRepository.findAllByOrderByIdAsc();
	        
			for(Jugador jugador: jugadores) {
	        	List<SorteoDiaria> sorteoDiarias = sorteoService.getActiveSorteosList(jugador);
	        	List<SorteoResponse> sorteosResponse = sorteoService.getSorteosResponses(sorteoDiarias, (User) jugador);
	        	Map<String, Double> values = processSorteoResponse(sorteosResponse);
	        	
	            JugadorResponse jugadorResponse = new JugadorResponse();
	           
	            jugadorResponse.setTotal(values.get("total"));
	            jugadorResponse.setComision(values.get("comision"));
	            jugadorResponse.setRiesgo(values.get("riesgo"));
	            
	            jugadorResponse.setMonedaType(jugador.getMoneda().toString());
	            jugadorResponse.setId(jugador.getId());
	            jugadorResponse.setBalance(jugador.getBalance());
	            jugadorResponse.setUsername(jugador.getUsername());
	            jugadorResponse.setName(jugador.getName());
	            List<Asistente> asistentes = asistenteRepository.findAllByJugador(jugador);
	            if (asistentes.size() > 0) {
	                List<AsistenteResponse> asistenteResponses = new ArrayList<>();
	                asistentes.forEach(asistente -> {
	                    AsistenteResponse asistenteResponse = new AsistenteResponse();
	                    asistenteResponse.setName(asistente.getName());
	                    asistenteResponse.setUsername(asistente.getUsername());
	                    asistenteResponse.setId(asistente.getId());
	                    asistenteResponses.add(asistenteResponse);
	                });
	                jugadorResponse.setAsistentes(asistenteResponses);
	            }
	            jugadorResponses.add(jugadorResponse);
	        }
		}catch(Exception e) {
			logger.debug(e.getMessage());
			throw e;
		}finally {
			logger.debug("getAllJugadores(): END");
		}
        return jugadorResponses;
	}
	
	public Map<String, Double> processSorteoResponse(List<SorteoResponse> sorteosResponse){
		Map<String,Double> result = new HashMap<>();
		
    	BigDecimal total = BigDecimal.ZERO;
    	BigDecimal comision = BigDecimal.ZERO;
    	BigDecimal riesgo = BigDecimal.ZERO;
    	
    	try {
    		logger.debug("processSorteoResponse(List<SorteoResponse> sorteosResponse): START");
    		for(SorteoResponse sorteo: sorteosResponse) {
        		total = total.add(BigDecimal.valueOf(sorteo.getTotal()));
        		comision = comision.add(BigDecimal.valueOf(sorteo.getComision()));
        		riesgo = riesgo.add(BigDecimal.valueOf(sorteo.getRiesgo()));
        	}
    		
        	result.put("total",total.doubleValue());
        	result.put("comision",comision.doubleValue());
        	result.put("riesgo", riesgo.doubleValue());
    	}catch(Exception e) {
    		logger.debug(e.getMessage());
    		throw e;
    	}finally {
    		logger.debug("processSorteoResponse(List<SorteoResponse> sorteosResponse): END");
    	}
    	
    	
		return result;
	}

	@Override
	public void submitBono(BonoRequest request, Long jugadorId) throws CanNotInsertBonoException, NotFoundException {
		try {
			logger.debug("submitBono(BonoRequest [weekid:{}], Long {}): START", request.getWeekId(), jugadorId);
			Jugador jugador = jugadorRepository.findById(jugadorId).orElseThrow(() -> new NotFoundException("Jugador not found"));
			Week week = weekRepository.findById(request.getWeekId()).orElseThrow(() -> new NotFoundException("Week not found"));
			Cambio cambio = cambioRepository.findFirstByOrderByIdDesc();
			Moneda moneda = monedaRepository.findByMonedaName(Util.getMonedaNameFromString(request.getMoneda()));
			
			User createdBy 	= userService.getLoggedInUser();
			Bono bono 		= new Bono();
			bono.setCambio(cambio);
			bono.setMoneda(moneda);
			bono.setBono(request.getBono());
			bono.setWeek(week);
			bono.setUser(jugador);
			bono.setCreatedBy(createdBy);
			
			historyService.validateIfJugadorIsElegibleForBono(jugador, week, bono);
			bonoRepository.save(bono);
				
		} catch (CanNotInsertBonoException | NotFoundException e) {
			logger.error("submitBono(BonoRequest [weekid:{}], Long {}): CATCH", request.getWeekId(), jugadorId);
			throw e;
		}finally {
			logger.debug("submitBono(BonoRequest request, Long jugadorId): END");
		}
	}
	
	
}











