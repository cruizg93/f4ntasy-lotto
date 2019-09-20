package com.devteam.fantasy.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.repository.AsistenteRepository;
import com.devteam.fantasy.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	AsistenteRepository asistenteRepository;
	
	public User getLoggedInUser(){
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		User loggedUser = null;
		if (principal instanceof UserDetails) {
			loggedUser = Optional.of(userRepository.getByUsername(((UserDetails) principal).getUsername()))
					.orElseThrow(() -> new UsernameNotFoundException("Error getting the logged in user."));
		}
		return loggedUser;
	}

	@Override
	public User getById(Long id) {
		return userRepository.getById(id);
	}

	@Override
	public User getByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username no existe."));
	}
	
	public List<Asistente> getJugadorAsistentes(Jugador jugador){
		return asistenteRepository.findAllByJugador(jugador);
	}
	
}