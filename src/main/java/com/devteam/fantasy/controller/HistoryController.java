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

import com.devteam.fantasy.message.response.SorteosPasadosWeek;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.service.HistoryService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/history")
public class HistoryController {

	@Autowired
	HistoryService historyService;
	
	@GetMapping("/weeks/")
	public List<Week> getWeeksList() {
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
}
