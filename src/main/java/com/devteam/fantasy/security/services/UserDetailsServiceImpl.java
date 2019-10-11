package com.devteam.fantasy.security.services;


import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.UserState;
import com.devteam.fantasy.repository.UserRepository;
import com.devteam.fantasy.service.AdminServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
	
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
    	
    	logger.debug("loadUserByUsername(String {}) START", username);
        User user = userRepository.findByUsername(username)
                	.orElseThrow(() -> 
                        new UsernameNotFoundException("User Not Found with -> username or email : " + username)
        );

        if(!user.getUserState().equals(UserState.ACTIVE)) {
        	throw new UsernameNotFoundException("User Inactive" + username);
        }
        
        logger.debug("loadUserByUsername(String {}) END", username);
        return UserPrinciple.build(user);
    }
    
}