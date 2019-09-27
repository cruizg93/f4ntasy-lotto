package com.devteam.fantasy.math;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.devteam.fantasy.message.response.SorteosPasadosWeek;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.NumeroGanador;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.repository.ApuestaRepository;
import com.devteam.fantasy.repository.AsistenteRepository;
import com.devteam.fantasy.repository.HistoricoBalanceRepository;
import com.devteam.fantasy.repository.NumeroGanadorRepository;
import com.devteam.fantasy.service.SorteoService;
import com.devteam.fantasy.service.SorteoServiceImpl;
import com.devteam.fantasy.service.UserService;
import com.devteam.fantasy.util.ApostadorName;
import com.devteam.fantasy.util.ChicaName;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.SorteoTypeName;
import com.devteam.fantasy.util.Util;

@Component
public class SorteoTotales {

	@Autowired
	ApuestaRepository apuestaRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private SorteoService sorteoService;

	@Autowired
	private NumeroGanadorRepository numeroGanadorRepository;
	
	private BigDecimal ventas;
	private BigDecimal comisiones;
	
	
	private MonedaName monedaName;
	private User user;
	private SorteoDiaria sorteoDiaria;

	public SorteoTotales() {
		
	}
	
	public void processSorteo(User user, SorteoDiaria sorteoDiaria) {
		MonedaName moneda = MonedaName.LEMPIRA;
		Jugador jugador = Util.getJugadorFromUser(user);
		if(jugador != null) {
			moneda = jugador.getMoneda().getMonedaName();
		}
		processSorteo(user, sorteoDiaria,moneda, false);
	}
	
	public void processSorteo(User user, SorteoDiaria sorteoDiaria, MonedaName monedaName, boolean skipAsistentes) {
		this.monedaName = monedaName;
		this.user = user;
		this.sorteoDiaria = sorteoDiaria;
		this.ventas = BigDecimal.ZERO;
		this.comisiones = BigDecimal.ZERO;
		
		Jugador jugador = Util.getJugadorFromUser(user);
		Set<Apuesta> apuestas =  apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
		if(user == null) {
			apuestas = sorteoDiaria.getApuestas();
		}
		
        for (Apuesta apuesta : apuestas) {
        	if(user == null ) { jugador = Util.getJugadorFromUser(apuesta.getUser()); }
        	
        	BigDecimal cantidad = MathUtil.getCantidadMultiplier(jugador, apuesta, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName(), this.monedaName);
			cantidad = cantidad.multiply(BigDecimal.valueOf(apuesta.getCantidad()));
            ventas = ventas.add(cantidad);
            comisiones = comisiones.add(BigDecimal.valueOf(apuesta.getComision()));
        }

        if (!skipAsistentes && user instanceof Jugador) {
        	List<Asistente> asistentes = userService.getJugadorAsistentes(jugador) ;
            BigDecimal cantidadAsistentesVentas = BigDecimal.ZERO;
            BigDecimal comisionAsistentesVentas = BigDecimal.ZERO;
            
            for(Asistente asistente: asistentes) {
            	Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
    			for(Apuesta apuesta : apuestaList) {
    				BigDecimal multiplier = MathUtil.getCantidadMultiplier(jugador, apuesta,sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName(), this.monedaName);
    				cantidadAsistentesVentas = multiplier.multiply(BigDecimal.valueOf(apuesta.getCantidad())).add(cantidadAsistentesVentas);
    				comisionAsistentesVentas = comisionAsistentesVentas.add(BigDecimal.valueOf(apuesta.getComision()));
    			}
            }
            ventas = ventas.add(cantidadAsistentesVentas);
            comisiones = comisiones.add(comisionAsistentesVentas);
        }
//        if( ventas.compareTo(BigDecimal.ZERO) != 0) {
//        	BigDecimal comisionRate = getComisionRate(jugador, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
//        	comisiones = ventas.multiply(comisionRate).divide(BigDecimal.valueOf(100));
//        }
	}
	
	public void processHitoricoApuestas(List<HistoricoApuestas> apuestas, List<Sorteo> sorteos, SorteosPasadosWeek sorteosPasadosWeek, MonedaName moneda) {
		BigDecimal ventas = BigDecimal.ZERO;
		BigDecimal comisiones = BigDecimal.ZERO;
		BigDecimal premios = BigDecimal.ZERO;
		
		//SorteoID, NumeroGanador
		Map<Long,Integer> numerosGanadores = new HashMap<Long,Integer>();
		for(Sorteo sorteo: sorteos) {
			Integer numero = numeroGanadorRepository.findBySorteo(sorteo);
			numerosGanadores.put(sorteo.getId(), numero);
		}
		for (HistoricoApuestas apuesta: apuestas) {
			double currencyExchange = MathUtil.getDollarChangeRate(Util.mapHistsoricoApuestaToApuesta(apuesta), moneda);
        	BigDecimal costo = BigDecimal.valueOf(apuesta.getCantidad()).multiply(BigDecimal.valueOf(apuesta.getCantidadMultiplier()));
            costo = costo.multiply(BigDecimal.valueOf(currencyExchange));
        	ventas = ventas.add(costo);
            
        	BigDecimal comision = BigDecimal.valueOf(apuesta.getComision()).multiply(BigDecimal.valueOf(currencyExchange));
            comisiones = comisiones.add(comision);
            
            if(numerosGanadores.containsKey(apuesta.getSorteo().getId())
            		&& numerosGanadores.get(apuesta.getSorteo().getId()) == apuesta.getNumero()) {
        		premios = premios.add(BigDecimal.valueOf(apuesta.getCantidad()).multiply(BigDecimal.valueOf(apuesta.getPremioMultiplier())));
            }
        }
		
		sorteosPasadosWeek.setComisiones(comisiones.toString());
		sorteosPasadosWeek.setVentas(ventas.toString());
		sorteosPasadosWeek.setSubTotal(ventas.subtract(comisiones).toString());
		sorteosPasadosWeek.setPremios(premios.toString());
	}
	
	//Need to return multiple values.
	@Deprecated 
	public BigDecimal getAsistentesVentasOnSorteo(Jugador jugador, SorteoDiaria sorteoDiaria) {
		List<Asistente> asistentes = userService.getJugadorAsistentes(jugador) ;
        BigDecimal cantidad = BigDecimal.ZERO;
        
        for(Asistente asistente: asistentes) {
        	Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
			for(Apuesta apuesta : apuestaList) {
				BigDecimal multiplier = MathUtil.getCantidadMultiplier(jugador, apuesta,sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName(), this.monedaName);
				cantidad = multiplier.multiply(BigDecimal.valueOf(apuesta.getCantidad())).add(cantidad);
			}
        }
        
        return cantidad;
	}
	
	public double getVentas() {
		return ventas.doubleValue();
	}
	
	public BigDecimal getVentasBD() {
		return ventas;
	}
	
	public double getComisiones() {
		return comisiones.doubleValue();
	}
	
	public BigDecimal getComisionesBD() {
		return comisiones;
	}
	
	public double getTotal() {
		return ventas.subtract(comisiones).doubleValue();
	}
	
	public BigDecimal getTotalBD() {
		return ventas.subtract(comisiones);
	}
	
	public void setMonedaName(MonedaName monedaName) {
		this.monedaName = monedaName;
	}

	public MonedaName getMonedaName() {
		return monedaName;
	}
	
}
