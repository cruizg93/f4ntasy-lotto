package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.exception.CanNotInsertBonoException;
import com.devteam.fantasy.message.request.BonoRequest;
import com.devteam.fantasy.message.response.ApuestasActivasResponse;
import com.devteam.fantasy.message.response.JugadorResponse;
import com.devteam.fantasy.message.response.JugadoresResponse;

import javassist.NotFoundException;

public interface AdminService {
	
	public JugadoresResponse getAllJugadores() throws Exception;

	public void submitBono(BonoRequest request, Long jugadorId) throws CanNotInsertBonoException, NotFoundException;
}
