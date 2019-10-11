package com.devteam.fantasy.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestAPIs {
	
	@GetMapping("/api/test/user")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MASTER') or hasRole('SUPERVISOR')")
	public String userAccess() {
		return ">>> User Contents!";
	}

	@GetMapping("/api/test/asist")
	@PreAuthorize("hasRole('ASIS') or hasRole('ADMIN') or hasRole('MASTER') or hasRole('SUPERVISOR')")
	public String projectManagementAccess() {
		return ">>> Board Management Project";
	}
	
	@GetMapping("/api/test/admin")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('SUPERVISOR')")
	public String adminAccess() {
		return ">>> Admin Contents";
	}

	@GetMapping("/api/test/master")
	@PreAuthorize("hasRole('MASTER') or hasRole('SUPERVISOR')")
	public String masterAccess() {
		return ">>> Master Contents";
	}
}