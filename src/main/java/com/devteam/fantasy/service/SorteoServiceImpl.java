package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Estado;
import com.devteam.fantasy.model.Jugador;
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
import com.devteam.fantasy.util.EstadoName;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.Util;

@Service
public class SorteoServiceImpl implements SorteoService{

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
	
	public List<SorteoDiaria> getPlayerSorteosList(User user) {
		Iterable<SorteoDiaria> sorteos = sorteoDiariaRepository.findAll();//BySorteoEstadoEstadoNot(EstadoName.CERRADA);
        for (SorteoDiaria sorteoDiaria : sorteos) {
        	Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
            sorteoDiaria.setApuestas(apuestas);
        }
        List<SorteoDiaria> target = StreamSupport.stream(sorteos.spliterator(), false)
        	    .collect(Collectors.toList());
        
        return target;
	}
	
	public List<SorteoResponse> getSorteosResponses(List<SorteoDiaria> sorteos, User user) {
        List<SorteoResponse> sorteoResponses = new ArrayList<>();
        Jugador jugador = null;
        MonedaName moneda = null;
        if(user instanceof Jugador){
            jugador = (Jugador) user;
            moneda= ((Jugador) user).getMoneda().getMonedaName();
        }else{
            jugador = ((Asistente) user).getJugador();
            moneda= ((Asistente) user).getJugador().getMoneda().getMonedaName();
        }
        
        for (SorteoDiaria sorteoDiaria : sorteos) {
        	BigDecimal total = new BigDecimal(0);
        	BigDecimal comision = new BigDecimal(0);
        	BigDecimal riesgo = new BigDecimal(0);
        	
        	String estado = sorteoDiaria.getSorteo().getEstado().getEstado().toString();
        	
        	for (Apuesta apuesta : sorteoDiaria.getApuestas()) {
            	BigDecimal cantidad = new BigDecimal(apuesta.getCantidad());
            	
                if(sorteoDiaria.getSorteo().isDiaria()){
                    if(jugador.getCostoMil()!=0){
                    	cantidad= cantidad.multiply(BigDecimal.valueOf(jugador.getCostoMil()));
                    }
                }else{
                    if(jugador.getCostoChicaMiles()!=0){
                    	cantidad= cantidad.multiply(BigDecimal.valueOf(jugador.getCostoChicaMiles()));
                    }else if(jugador.getCostoChicaPedazos()!=0){
                    	cantidad= cantidad.multiply(BigDecimal.valueOf(jugador.getCostoChicaPedazos()));
                    }
                }
                total = total.add(cantidad);
            }  
            
            if(sorteoDiaria.getApuestas().size()>0) {
            	if(sorteoDiaria.getSorteo().isDiaria()){
                    comision = BigDecimal.valueOf(jugador.getComisionDirecto());
                }else{
                    comision = BigDecimal.valueOf(jugador.getComisionChicaDirecto() + jugador.getComisionChicaPedazos());
                }
            	comision = comision.multiply(total).divide(BigDecimal.valueOf(100));
            	riesgo = total.subtract(comision);
            }
            
            sorteoResponses.add(new SorteoResponse(sorteoDiaria.getId(),
                    Util.formatTimestamp2String(sorteoDiaria.getSorteoTime()),
                    Util.getDayFromTimestamp(sorteoDiaria.getSorteoTime()),
                    Util.getHourFromTimestamp(sorteoDiaria.getSorteoTime()),
                    total.doubleValue(), comision.doubleValue(), riesgo.doubleValue(), 
                    estado, moneda.toString(), sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString()));
        }
        
        
        
        
        return sorteoResponses;
	}
	
	public BigDecimal getAsistentesTotalBySorteo(Jugador jugador,SorteoDiaria sorteoDiaria) {
		BigDecimal total = new BigDecimal(0);
		List<Asistente> asistentes = asistenteRepository.findAllByJugador(jugador);
		
		BigDecimal cantidad = BigDecimal.valueOf(asistentes.stream().map(asistente ->
					        apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente))
					        .filter(apuestaList -> apuestaList.size() > 0)
					        .flatMap(Collection::stream)
					        .mapToDouble(Apuesta::getCantidad)
					        .sum());;
					        
        if(sorteoDiaria.getSorteo().isDiaria()) {
            if(jugador.getCostoMil()!=0){
            	cantidad.multiply(BigDecimal.valueOf(jugador.getCostoMil()));
            }
        }else{
            if(jugador.getCostoChicaMiles()!=0){
            	cantidad.multiply(BigDecimal.valueOf(jugador.getCostoChicaMiles()));
            }else if(jugador.getCostoChicaPedazos()!=0){
            	cantidad.multiply(BigDecimal.valueOf(jugador.getCostoChicaPedazos()));
            }
        }
        total.add(cantidad);
		return total;
	}

	@Override
	public void setNumeroGanador(Long id, int numeroGanador) {
		// TODO Auto-generated method stub
		
	}
}



















