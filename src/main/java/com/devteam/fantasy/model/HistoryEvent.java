package com.devteam.fantasy.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.devteam.fantasy.util.HistoryEventType;

@Entity
public class HistoryEvent {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Enumerated(EnumType.STRING)
    @Column(length = 60)
	private HistoryEventType eventType;
	
	@ManyToOne
    @JoinColumn(name = "created_by")
	private User user;

	private Timestamp createdDate;
	
	private String oldValue;
	
	private String newValue;

	/*
	 * This value is the fk for the owner of this event.
	 * ex. user_id(jugador_id) if the player was created, locked or edited
	 * sorteo_id if the sorteo get a winning number update etc etc etc 
	 */
	private Long keyValue;
	
	public HistoryEvent(HistoryEventType eventType, User user) {
		super();
		this.eventType = eventType;
		this.user = user;
	}

	public HistoryEvent(HistoryEventType eventType, User user, String oldValue,String newValue) {
		super();
		this.eventType = eventType;
		this.user = user;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public HistoryEventType getEventType() {
		return eventType;
	}

	public User getUser() {
		return user;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setEventType(HistoryEventType eventType) {
		this.eventType = eventType;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public Timestamp getCreatedDate() {
		return Timestamp.valueOf(LocalDateTime.now());
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public Long getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(Long keyValue) {
		this.keyValue = keyValue;
	}
}
	
