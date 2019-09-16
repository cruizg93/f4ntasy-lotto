package com.devteam.fantasy.service;

import com.devteam.fantasy.model.HistoryEvent;
import com.devteam.fantasy.util.HistoryEventType;

public interface HistoryService {

	public HistoryEvent createEvent(HistoryEventType eventType, String message); 
}
