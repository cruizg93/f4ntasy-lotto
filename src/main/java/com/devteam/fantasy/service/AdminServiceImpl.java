package com.devteam.fantasy.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.repository.ApuestaRepository;
import com.devteam.fantasy.repository.NumeroGanadorRepository;
import com.devteam.fantasy.repository.ResultadoRepository;
import com.devteam.fantasy.repository.SorteoRepository;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.Util;

@Service
public class AdminServiceImpl implements AdminService{

	@Autowired
	private SorteoService sorteoService;
	
	@Autowired
    SorteoRepository sorteoRepository;
	
	@Autowired
    NumeroGanadorRepository numeroGanadorRepository;
	
	@Autowired	
    ResultadoRepository resultadoRepository;
	
	@Autowired
    ApuestaRepository apuestaRepository;
	
	@Override
	public List<ApuestasActivasResponse> getSorteosListWithMoneda(String currency) {
		List<SorteoDiaria> sorteoDiarias = sorteoService.getActiveSorteosList();
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
		
        Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);

        for(Apuesta apuesta: apuestas){
        	BigDecimal cambio= Util.getApuestaCambio(currency, apuesta);
            
            BigDecimal costoMilChica = BigDecimal.ONE;   
            BigDecimal costoMilDiaria = BigDecimal.ONE;
            BigDecimal costoPedazoChica = BigDecimal.ONE;
            
            Jugador jugador = Util.getJugadorFromApuesta(apuesta);
            if(Util.isSorteoTypeDiaria(sorteoDiaria.getSorteo())){
                costoMilDiaria = new BigDecimal(jugador.getCostoMil() != 0 ? jugador.getCostoMil() : 1 );
            }else{
                costoMilChica = new BigDecimal( jugador.getCostoChicaMiles() != 0 ? jugador.getCostoChicaMiles() : 1) ;
                costoPedazoChica = new BigDecimal( jugador.getCostoChicaPedazos() != 0 ? jugador.getCostoChicaPedazos() : 1) ;
            }
            total = total.add(new BigDecimal(apuesta.getCantidad()).multiply(cambio).multiply(costoMilChica).multiply(costoMilDiaria).multiply(costoPedazoChica));
            comision = comision.add(new BigDecimal(apuesta.getComision()).multiply(cambio) );
        }
        
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

}











