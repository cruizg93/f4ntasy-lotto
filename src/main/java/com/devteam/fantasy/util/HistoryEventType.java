package com.devteam.fantasy.util;

public enum HistoryEventType {
	BET_SUBMITTED("Apuesta"),
    BET_ALL_DELETED("Todas las Apuestas Eliminadas"),
    BET_DELETED("Apuesta Eliminadas"),
	BONO_CREATED("Bono"),
	PLAYER_CREATED("Vendedor Creado"),
	PLAYER_DELETED("Vendedor Eliminidao"),
    PLAYER_EDITED("Vendedor Editado"),
    PLAYER_PASSWORD_CHANGED("Constrasena Cambiada"),
    SORTEO_LOCKED("Sorteo Bloqueado"),
    SORTEO_UNLOCKED("Sorteo Desbloqueado"),
    SORTEO_CLOSED("Sorteo Cerrado"),
    SORTEO_REOPEN("Sorteo Re-Abierto"),
    WEEK_CLOSED("Semana Cerrada"),
    WINNING_NUMBER("Numero Ganador"),
    WINNING_NUMBER_CHANGED("Cambio de Numero Ganador"),
    ;
	
    
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
