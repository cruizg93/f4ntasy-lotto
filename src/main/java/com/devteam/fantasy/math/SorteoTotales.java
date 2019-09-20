package com.devteam.fantasy.math;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.repository.ApuestaRepository;
import com.devteam.fantasy.repository.AsistenteRepository;
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
	
	private BigDecimal ventas;
	private BigDecimal comisiones;
	
	
	private MonedaName monedaName;
	private User user;
	private SorteoDiaria sorteoDiaria;

	public void setMonedaName(MonedaName monedaName) {
		this.monedaName = monedaName;
	}
	
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
        	
        	BigDecimal cantidad = getCantidadMultiplier(jugador, apuesta, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName()).multiply(BigDecimal.valueOf(apuesta.getCantidad()));
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
    				BigDecimal multiplier = getCantidadMultiplier(jugador, apuesta,sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
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
	
	private double getDollarChangeRate(Apuesta apuesta) {
		Jugador jugador;
		double cambio = 1d;
		if(apuesta.getUser() instanceof Jugador){
			jugador =  (Jugador) apuesta.getUser();
		}else {
			jugador =  ((Asistente) apuesta.getUser()).getJugador();
		}
        if(this.monedaName.toString().equalsIgnoreCase("lempira") && jugador.getMoneda().getMonedaName().equals(MonedaName.DOLAR)){
            cambio = apuesta.getCambio().getCambio();
        }else if(this.monedaName.toString().equalsIgnoreCase("dolar") && jugador.getMoneda().getMonedaName().equals(MonedaName.LEMPIRA)){
            cambio = 1/apuesta.getCambio().getCambio();
        }
        return cambio;
	}

	public BigDecimal getCantidadMultiplier(Jugador jugador, Apuesta apuesta, SorteoTypeName sorteoType) {
		BigDecimal cantidad =BigDecimal.ONE;
		
		if(sorteoType.equals(SorteoTypeName.DIARIA)){
        	if(jugador.getTipoApostador().getApostadorName().equals(ApostadorName.MILES)){
            	cantidad= BigDecimal.valueOf(jugador.getCostoMil());
            }
        }else if(jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)){
        	cantidad= BigDecimal.valueOf(jugador.getCostoChicaMiles());
        }else if(jugador.getTipoChica().getChicaName().equals(ChicaName.PEDAZOS)){
        	cantidad= BigDecimal.valueOf(jugador.getCostoChicaPedazos());
        }
		
		double cambioRate = getDollarChangeRate(apuesta);
		
		return cantidad.multiply(BigDecimal.valueOf(cambioRate));
	}
	
	public BigDecimal getComisionRate(Jugador jugador, SorteoTypeName sorteoType) {
		BigDecimal comisionRate =BigDecimal.ONE;
		if(sorteoType.equals(SorteoTypeName.DIARIA)){
			comisionRate = BigDecimal.valueOf(jugador.getComisionDirecto());
	    }else if(sorteoType.equals(SorteoTypeName.CHICA)){
	    	comisionRate= BigDecimal.valueOf(jugador.getComisionChicaDirecto() + jugador.getComisionChicaPedazos());
	    }
		return comisionRate;
	}
	
	//Need to return multiple values.
	@Deprecated 
	public BigDecimal getAsistentesVentasOnSorteo(Jugador jugador, SorteoDiaria sorteoDiaria) {
		List<Asistente> asistentes = userService.getJugadorAsistentes(jugador) ;
        BigDecimal cantidad = BigDecimal.ZERO;
        
        for(Asistente asistente: asistentes) {
        	Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
			for(Apuesta apuesta : apuestaList) {
				BigDecimal multiplier = getCantidadMultiplier(jugador, apuesta,sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
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
}
