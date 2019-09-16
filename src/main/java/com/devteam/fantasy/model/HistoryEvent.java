package com.devteam.fantasy.model;

import java.sql.Timestamp;

import com.devteam.fantasy.util.HistoryEventType;

public class HistoryEvent {

	private HistoryEventType eventType;
	
	private User user;

	private Timestamp createdDate;
	
	private String message;

	
	public HistoryEvent(HistoryEventType eventType, String message) {
		super();
		this.eventType = eventType;
		this.message = message;
	}

	public HistoryEventType getEventType() {
		return eventType;
	}

	public void setEventType(HistoryEventType eventType) {
		this.eventType = eventType;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
