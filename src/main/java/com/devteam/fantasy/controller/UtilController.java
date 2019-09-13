package com.devteam.fantasy.controller;


import com.devteam.fantasy.message.request.LoginForm;
import com.devteam.fantasy.message.response.TimeResponse;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.repository.UserRepository;
import com.devteam.fantasy.schedule.ScheduledTasks;
import com.devteam.fantasy.util.Util;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/utils")
public class UtilController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    ScheduledTasks scheduler;

    @GetMapping("/time")
    public TimeResponse getTime(){
        TimeResponse timeResponse=new TimeResponse();
        timeResponse.setTime(Util.formatDate2StringTime());
        timeResponse.setFormatDate(Util.formatDate2StringDate());
        return timeResponse;
    }

    @PostMapping("/password/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('USER')or hasRole('ASIS')")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody LoginForm loginForm){
        User user=userRepository.getByUsername(loginForm.getUsername());
        user.setPassword(encoder.encode(loginForm.getPassword()));
        user.setNoFirstConnection(true);
        userRepository.save(user);
        return ResponseEntity.ok("Update user password");
    }

    @PostMapping("/connection/first")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('USER')or hasRole('ASIS')")
    public boolean getFirstConnection(@Valid @RequestBody ObjectNode json){
        return Util.getUserFromJsonNode(userRepository, json).isNoFirstConnection();
    }

    @GetMapping("/task/11")
    public void excuteTask11() {
    	scheduler.updateSorteoDiaria11();
    }
    @GetMapping("/task/15")
    public void excuteTask15() {
    	scheduler.updateSorteoDiaria15();
    }
    @GetMapping("/task/21")
    public void excuteTask21() {
    	scheduler.updateSorteoDiaria21();
    }
}
