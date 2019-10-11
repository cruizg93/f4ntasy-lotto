package com.devteam.fantasy.controller;


import com.devteam.fantasy.message.request.LoginForm;
import com.devteam.fantasy.message.request.SignUpForm;
import com.devteam.fantasy.message.response.JwtResponse;
import com.devteam.fantasy.message.response.JwtResponseCustom;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Role;
import com.devteam.fantasy.util.RoleName;
import com.devteam.fantasy.util.Util;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.UserState;
import com.devteam.fantasy.repository.RoleRepository;
import com.devteam.fantasy.repository.UserRepository;
import com.devteam.fantasy.security.jwt.JwtProvider;
import com.devteam.fantasy.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthRestAPIs {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtProvider jwtProvider;



    @GetMapping("/test")
    public String getTest(){
        return "API Working";
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {
        try {
        	User user = userRepository.getByUsername(loginRequest.getUsername());
        	if(user != null && !user.getUserState().equals(UserState.ACTIVE)) {
        		throw new Exception("Usuario not active"); 
        	}
        	
        	Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtProvider.generateJwtToken(authentication);
            JwtResponseCustom response = new JwtResponseCustom(jwt, loginRequest.getUsername());
            response.setUserId(String.valueOf(user.getId()));
            response.setFirstConnection(user.isFirstConnection());
            
            if( user instanceof Jugador || user instanceof Asistente) {
            	Jugador jugador = Util.getJugadorFromUser(user);
            	response.setCurrency(jugador.getMoneda().getMonedaName().toString());
            	response.setCurrencySymbol(Util.getMonedaSymbolFromMonedaName(jugador.getMoneda().getMonedaName()));
            }
        	Set<RoleName> roles = new HashSet<>();
        	for(Role role: user.getRoles()) {
        		roles.add(role.getName());
        	}
        	response.setRoles(roles);
            
            return ResponseEntity.ok(response);
        }catch(Exception e) {
        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario o Contraseña incorrectos");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<String>("Fail -> Username is already taken!",
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        AdminController.setRoles(strRoles, roles, roleRepository);

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok().body("User registered successfully!");
    }
}