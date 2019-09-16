package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorResponse;

public interface AdminService {
	
	public List<JugadorResponse> getAllJugadores();

}
