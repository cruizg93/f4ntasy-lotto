package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.exception.CanNotInsertBonoException;
import com.devteam.fantasy.message.request.BonoRequest;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorResponse;

import javassist.NotFoundException;

public interface AdminService {
	
	public List<JugadorResponse> getAllJugadores();

	public void submitBono(BonoRequest request, Long jugadorId) throws CanNotInsertBonoException, NotFoundException;
}
