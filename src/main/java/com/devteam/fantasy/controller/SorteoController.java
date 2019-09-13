package com.devteam.fantasy.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devteam.fantasy.message.response.ApuestaActivaResumenResponse;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.repository.SorteoDiariaRepository;
import com.devteam.fantasy.service.AdminService;
import com.devteam.fantasy.service.SorteoService;
import com.devteam.fantasy.util.TuplaRiesgo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/sorteos")
public class SorteoController {

	@Autowired
    private SorteoService sorteoService;
    
    @Autowired
    private AdminService adminService;
	
	@GetMapping("/activos/{moneda}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<ApuestasActivasResponse> getApuestasActivas(@PathVariable String moneda) {
    	return adminService.getSorteosListWithMoneda(moneda);
    }
	
	@PostMapping("/activos/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApuestaActivaResumenResponse getDetallesApuestasActivasById(@PathVariable Long id, @Valid @RequestBody ObjectNode json) {
		ObjectMapper mapper = new ObjectMapper();
        String currency = mapper.convertValue(json.get("type"), String.class);
    	return sorteoService.getActiveSorteoDetail(id, currency);
	}
}










