package com.devteam.fantasy.math;

import java.math.BigDecimal;

import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.util.ApostadorName;
import com.devteam.fantasy.util.ChicaName;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.SorteoTypeName;

public class MathUtil {

	private MathUtil() {
	}
	
	
	public static BigDecimal getComisionRate(Jugador jugador, SorteoTypeName sorteoType) {
		BigDecimal comisionRate =BigDecimal.ONE;
		
		if(sorteoType.equals(SorteoTypeName.DIARIA)){
			comisionRate = BigDecimal.valueOf(jugador.getComisionDirecto());
	    }else if(sorteoType.equals(SorteoTypeName.CHICA)){
	    	comisionRate= BigDecimal.valueOf(jugador.getComisionChicaDirecto() + jugador.getComisionChicaPedazos());
	    }
		return comisionRate;
	}
	
	
	public static BigDecimal getCantidadMultiplier(Jugador jugador, Apuesta apuesta, SorteoTypeName sorteoType, MonedaName currencyRequested) {
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
		
		double cambioRate = getDollarChangeRate(apuesta, currencyRequested);
		
		return cantidad.multiply(BigDecimal.valueOf(cambioRate));
	}
	
	public static double getDollarChangeRate(Apuesta apuesta, MonedaName currencyRequested) {
		Jugador jugador;
		double cambio = 1d;
		if(apuesta.getUser() instanceof Jugador){
			jugador =  (Jugador) apuesta.getUser();
		}else {
			jugador =  ((Asistente) apuesta.getUser()).getJugador();
		}
        if(currencyRequested.toString().equalsIgnoreCase("lempira") && jugador.getMoneda().getMonedaName().equals(MonedaName.DOLAR)){
            cambio = apuesta.getCambio().getCambio();
        }else if(currencyRequested.toString().equalsIgnoreCase("dolar") && jugador.getMoneda().getMonedaName().equals(MonedaName.LEMPIRA)){
            cambio = 1/apuesta.getCambio().getCambio();
        }
        return cambio;
	}
	
}
