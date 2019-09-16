package com.devteam.fantasy.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devteam.fantasy.model.HistoryEvent;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.util.HistoryEventType;

@Service
public class HistoryServiceImpl implements HistoryService {

	@Autowired
	UserService userService;
	
	@Override
	public HistoryEvent createEvent(HistoryEventType eventType, String message) {
		HistoryEvent event = new HistoryEvent(eventType, message);
		User user = userService.getLoggedInUser();
		Timestamp createdDate = null;
		event.setUser(user);
		event.setCreatedDate(createdDate);
				
				
		return null;
	}

}
