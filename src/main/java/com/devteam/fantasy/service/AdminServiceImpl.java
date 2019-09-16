package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.AsistenteResponse;
import com.devteam.fantasy.message.response.JugadorResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.repository.ApuestaRepository;
import com.devteam.fantasy.repository.AsistenteRepository;
import com.devteam.fantasy.repository.JugadorRepository;
import com.devteam.fantasy.repository.NumeroGanadorRepository;
import com.devteam.fantasy.repository.ResultadoRepository;
import com.devteam.fantasy.repository.SorteoDiariaRepository;
import com.devteam.fantasy.repository.SorteoRepository;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.SorteoTypeName;
import com.devteam.fantasy.util.Util;

@Service
public class AdminServiceImpl implements AdminService{

	@Autowired
	private SorteoService sorteoService;
	
	@Autowired
    SorteoRepository sorteoRepository;
	
	@Autowired
    JugadorRepository jugadorRepository;
	
	@Autowired
    AsistenteRepository asistenteRepository;
	
	@Autowired
    SorteoDiariaRepository sorteoDiariaRepository;
	
	@Autowired
    NumeroGanadorRepository numeroGanadorRepository;
	
	@Autowired	
    ResultadoRepository resultadoRepository;
	
	@Autowired
    ApuestaRepository apuestaRepository;
	
	
	public List<JugadorResponse> getAllJugadores(){
		List<JugadorResponse> jugadorResponses = new ArrayList<>();
        List<Jugador> jugadores = jugadorRepository.findAllByOrderByIdAsc();
        
        jugadores.forEach(jugador -> {	
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
        });
        return jugadorResponses;
	}
	
	public Map<String, Double> processSorteoResponse(List<SorteoResponse> sorteosResponse){
		Map<String,Double> result = new HashMap<>();
		
    	BigDecimal total = BigDecimal.ZERO;
    	BigDecimal comision = BigDecimal.ZERO;
    	BigDecimal riesgo = BigDecimal.ZERO;
    	
    	for(SorteoResponse sorteo: sorteosResponse) {
    		total = total.add(BigDecimal.valueOf(sorteo.getTotal()));
    		comision = comision.add(BigDecimal.valueOf(sorteo.getComision()));
    		riesgo = riesgo.add(BigDecimal.valueOf(sorteo.getRiesgo()));
    	}
		
    	result.put("total",total.doubleValue());
    	result.put("comision",comision.doubleValue());
    	result.put("riesgo", riesgo.doubleValue());
		return result;
	}
	
}











