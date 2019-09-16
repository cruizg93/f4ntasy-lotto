package com.devteam.fantasy.controller;

import java.util.List;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorSorteosResponse;
import com.devteam.fantasy.message.response.SorteoResponse;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.service.AdminService;
import com.devteam.fantasy.service.SorteoService;
import com.devteam.fantasy.service.UserService;
import com.devteam.fantasy.util.Util;
import com.fasterxml.jackson.databind.node.ObjectNode;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/sorteos")
public class SorteoController {

	@Autowired
    private SorteoService sorteoService;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserService userService;
	
	@GetMapping("/activos/{moneda}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<ApuestasActivasResponse> getApuestasActivas(@PathVariable String moneda) {
    	return sorteoService.getSorteosListWithMoneda(moneda);
    }
	
	@GetMapping("/activosResumen/judadores/{id}/{currency}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApuestaActivaResumenResponse getSorteosActivosResumteByIdAndCurrency(@PathVariable Long id, @PathVariable String currency, @Valid @RequestBody ObjectNode json) {
    	return sorteoService.getActiveSorteoDetail(id, currency);
	}
	
	@GetMapping("/activos/jugadores/{username}")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public List<SorteoResponse> getSorteosActivosByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
    	List<SorteoDiaria> sorteos = sorteoService.getActiveSorteosList(user);
    	List<SorteoResponse> sorteosResponses = sorteoService.getSorteosResponses(sorteos, user);    	
    	return sorteosResponses;
	}
	
	@GetMapping("/activosResumen/judadores/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public JugadorSorteosResponse findTodaySorteobyUsername(@PathVariable String username) {
		User user = userService.getByUsername(username);
		JugadorSorteosResponse jugadorSorteosResponse = new JugadorSorteosResponse();
        jugadorSorteosResponse.setName(user.getName());
        jugadorSorteosResponse.setMoneda(Util.getJugadorFromUser(user).getMoneda().getMonedaName().toString());
        
        List<SorteoDiaria> sorteosDiarios = sorteoService.getActiveSorteosList(user);
        jugadorSorteosResponse.setSorteos(sorteoService.getSorteosResponses(sorteosDiarios, user));
        return jugadorSorteosResponse;
	}
	
}










