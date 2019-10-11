package com.devteam.fantasy.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Role;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.UserState;
import com.devteam.fantasy.repository.AsistenteRepository;
import com.devteam.fantasy.repository.RoleRepository;
import com.devteam.fantasy.repository.UserRepository;
import com.devteam.fantasy.util.RoleName;

@Service
public class UserServiceImpl implements UserService{
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	AsistenteRepository asistenteRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	private User loggedUser;
	
	public User getLoggedInUser(){
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		if (principal instanceof UserDetails) {
			UserDetails userDetails = (UserDetails)principal;
			
			if(loggedUser == null || !userDetails.getUsername().equalsIgnoreCase(loggedUser.getUsername()) ){
				loggedUser = Optional.of(userRepository.getByUsername(((UserDetails) principal).getUsername()))
						.orElseThrow(() -> new UsernameNotFoundException("Error getting the logged in user."));
			}
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
		return asistenteRepository.findAllByJugadorAndUserState(jugador, UserState.ACTIVE);
	}

	@Override
	public boolean verifyAdminPassword(String password) {
		User logged = getLoggedInUser();
		if( passwordEncoder.matches(password, logged.getPassword())) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isUserMasterRole(User user) {
		Role master = roleRepository.findByName(RoleName.ROLE_MASTER).get();
		
		return user.getRoles().stream().anyMatch(x -> x.equals(master));
	}
	
	@Override
	public boolean isUserAdminRole(User user) {
		Role master = roleRepository.findByName(RoleName.ROLE_ADMIN).get();
		
		return user.getRoles().stream().anyMatch(x -> x.equals(master));
	}

	@Override
	public boolean isUserSupervisorRole(User user) {
		Role master = roleRepository.findByName(RoleName.ROLE_SUPERVISOR).get();
		
		return user.getRoles().stream().anyMatch(x -> x.equals(master));
	}
}
