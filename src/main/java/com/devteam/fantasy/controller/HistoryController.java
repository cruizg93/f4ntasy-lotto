package com.devteam.fantasy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devteam.fantasy.message.response.NumeroGanadorSorteoResponse;
import com.devteam.fantasy.message.response.SorteosPasadosApuestas;
import com.devteam.fantasy.message.response.SorteosPasadosJugador;
import com.devteam.fantasy.message.response.SorteosPasadosWeek;
import com.devteam.fantasy.message.response.WeekResponse;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.service.HistoryService;
import com.devteam.fantasy.service.UserService;
import com.devteam.fantasy.util.Util;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/history")
public class HistoryController {

	@Autowired
	HistoryService historyService;
	
	@Autowired
	UserService userService;
	
	@GetMapping("/numeros/ganadores/{currency}")
	public ResponseEntity<List<NumeroGanadorSorteoResponse>> numerosGanadores(@PathVariable String currency){
		List<NumeroGanadorSorteoResponse> result = null;
		try {
			result = historyService.getNumerosGanadores(currency);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);			
		}
		return new ResponseEntity<List<NumeroGanadorSorteoResponse>>(result,HttpStatus.OK);
	}
	
	@GetMapping("/weeks")
	public List<WeekResponse> getWeeksList() {
		return historyService.getAllWeeks();
	}
	
	@GetMapping("/weeks/{id}/{moneda}")
	public ResponseEntity<SorteosPasadosWeek> getWeekOverview(@PathVariable Long id, @PathVariable String moneda) {
		SorteosPasadosWeek result = null;
		try {
			result = historyService.getSorteosPasadosByWeek(id, moneda);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<SorteosPasadosWeek>(result,HttpStatus.OK);
	}
	
	@GetMapping("/weeks/{id}/jugador")
	public ResponseEntity<SorteosPasadosJugador> getWeekOverviewByJugador(@PathVariable Long id){
		SorteosPasadosJugador result = null;
		try {
			User user = userService.getLoggedInUser();
			Jugador jugador = Util.getJugadorFromUser(user);
			if(jugador != null) {
				result = historyService.getSorteosPasadosJugadorByWeek(id, jugador);
			}
			
		} catch (Exception e) {
			return new ResponseEntity<SorteosPasadosJugador>(result,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<SorteosPasadosJugador>(result,HttpStatus.OK);
	}
	
	@GetMapping("/weeks/jugador/sorteo/{id}")
	public ResponseEntity<SorteosPasadosApuestas> getApuestasOverviewBySorteo(@PathVariable Long id){
		SorteosPasadosApuestas result = null;
		try {
			User user = userService.getLoggedInUser();
			Jugador jugador = Util.getJugadorFromUser(user);
			if(jugador != null) {
				result = historyService.getApuestasPasadasBySorteoAndJugador(id, jugador);
			}
		} catch (Exception e) {
			return new ResponseEntity<SorteosPasadosApuestas>(result,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<SorteosPasadosApuestas>(result,HttpStatus.OK);
	}
}
