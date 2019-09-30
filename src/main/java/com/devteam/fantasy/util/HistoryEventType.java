package com.devteam.fantasy.util;

public enum HistoryEventType {
	PLAYER_CREATED("Vendedor Creado"),
	PLAYER_DELETED("Vendedor Eliminidao"),
    PLAYER_EDITED("Vendedor Editado"),
    PLAYER_PASSWORD_CHANGED("Constrasena Cambiada"),
    BET_DELETED("Apuestas Eliminadas"),
    SORTEO_LOCKED("Sorteo Bloqueado"),
    WINNING_NUMBER("Numero Ganador"),
    WEEK_CLOSED("Semana Cerrada");
	
    
    private String label;
    HistoryEventType(String label) {
    	this.label = label;
    }
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}
