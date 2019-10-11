package com.devteam.fantasy.service;

import java.util.List;

import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.User;

public interface UserService {
	
	public boolean verifyAdminPassword(String password) ;

	public User getLoggedInUser();
	
	public User getById(Long id);

	public User getByUsername(String username);
	
	public List<Asistente> getJugadorAsistentes(Jugador jugador);
	
	public boolean isUserMasterRole(User user);

	public boolean isUserAdminRole(User user);
	
	public boolean isUserSupervisorRole(User user);
}
