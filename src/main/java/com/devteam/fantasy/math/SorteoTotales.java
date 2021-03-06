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

import com.devteam.fantasy.message.response.SorteosPasadosJugadores;
import com.devteam.fantasy.message.response.SummaryResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Moneda;
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
	private BigDecimal premios;
	private BigDecimal cantidades;
	
	public BigDecimal getCantidades() {
		return cantidades;
	}

	public void setCantidades(BigDecimal cantidades) {
		this.cantidades = cantidades;
	}

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
		this.premios = BigDecimal.ZERO;
		this.cantidades = BigDecimal.ZERO;
		
		Jugador jugador = Util.getJugadorFromUser(user);
		Set<Apuesta> apuestas; 
		if(user == null) {
			apuestas = sorteoDiaria.getApuestas();
		}else {
			apuestas =  apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
		}
		
        for (Apuesta apuesta : apuestas) {
        	if(user == null ) { jugador = Util.getJugadorFromUser(apuesta.getUser()); }
        	cantidades = cantidades.add(BigDecimal.valueOf(apuesta.getCantidad()));
        	
        	BigDecimal cantidad = MathUtil.getCantidadMultiplier(jugador, apuesta, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName(), this.monedaName);
			cantidad = cantidad.multiply(BigDecimal.valueOf(apuesta.getCantidad()));
            ventas = ventas.add(cantidad);

            BigDecimal comisionRate = MathUtil.getComisionRate(jugador, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
			comisionRate = comisionRate.divide(BigDecimal.valueOf(100));
			BigDecimal comisionApuesta = comisionRate.multiply(cantidad);				
			
            comisiones = comisiones.add(comisionApuesta);

            BigDecimal currencyExchange = MathUtil.getDollarChangeRate(apuesta, monedaName);
            BigDecimal premio = MathUtil.getPremioFromApuesta(jugador, apuesta,sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
			premio = premio.multiply(currencyExchange);
        }

        if (!skipAsistentes && user instanceof Jugador) {
        	List<Asistente> asistentes = userService.getJugadorAsistentes(jugador) ;
            BigDecimal cantidadAsistentesVentas = BigDecimal.ZERO;
            BigDecimal comisionAsistentesVentas = BigDecimal.ZERO;
            
            for(Asistente asistente: asistentes) {
            	Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
    			for(Apuesta apuesta : apuestaList) {
    				cantidades = cantidades.add(BigDecimal.valueOf(apuesta.getCantidad()));
    				BigDecimal multiplier = MathUtil.getCantidadMultiplier(jugador, apuesta,sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName(), this.monedaName);
    				BigDecimal costoApuesta = multiplier.multiply(BigDecimal.valueOf(apuesta.getCantidad()));
    				
    				cantidadAsistentesVentas = cantidadAsistentesVentas.add(multiplier.multiply(BigDecimal.valueOf(apuesta.getCantidad())));

    				BigDecimal comisionRate = MathUtil.getComisionRate(jugador, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName());
					comisionRate = comisionRate.divide(BigDecimal.valueOf(100));
					BigDecimal comision = comisionRate.multiply(costoApuesta);
					
    				comisionAsistentesVentas = comisionAsistentesVentas.add(comision);
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
	
	public SummaryResponse processHitoricoApuestas(List<HistoricoApuestas> apuestas, String currencyRequested) {
		SummaryResponse summary = new SummaryResponse();
		BigDecimal ventas = BigDecimal.ZERO;
		BigDecimal comisiones = BigDecimal.ZERO;
		BigDecimal premios = BigDecimal.ZERO;
		MonedaName moneda = Util.getMonedaNameFromString(currencyRequested);

		for (HistoricoApuestas apuesta: apuestas) {
			NumeroGanador numero = numeroGanadorRepository.getBySorteo(apuesta.getSorteo());

			BigDecimal currencyExchange = MathUtil.getDollarChangeRate(Util.mapHistsoricoApuestaToApuesta(apuesta), moneda);
        	
			BigDecimal costo = BigDecimal.valueOf(apuesta.getCantidad()).multiply(BigDecimal.valueOf(apuesta.getCantidadMultiplier()));
            costo = costo.multiply(currencyExchange);
        	ventas = ventas.add(costo);
            
        	BigDecimal comision = BigDecimal.valueOf(apuesta.getComisionMultiplier()).multiply(costo);
            comisiones = comisiones.add(comision);
            
            if(numero.getNumeroGanador() == apuesta.getNumero()) {
        		BigDecimal premio = BigDecimal.valueOf(apuesta.getCantidad()).multiply(BigDecimal.valueOf(apuesta.getPremioMultiplier()));
        		premio = premio.multiply( currencyExchange);
        		premios = premios.add(premio);
            }
        }
		
		summary.setCurrency(moneda.name());
		summary.setComisiones(comisiones.doubleValue());
		summary.setVentas(ventas.doubleValue());
		summary.setSubTotal(ventas.subtract(comisiones).doubleValue());
		summary.setPremios(premios.doubleValue());
		summary.setPerdidasGanas(summary.getSubTotal() -summary.getPremios() - summary.getBonos() );
		return summary;
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

	public BigDecimal getPremio() {
		return premios;
	}

	public void setPremio(BigDecimal premio) {
		this.premios = premio;
	}
	
}
