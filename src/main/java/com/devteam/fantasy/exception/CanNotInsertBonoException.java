package com.devteam.fantasy.exception;

public class CanNotInsertBonoException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CanNotInsertBonoException(Long weekId, Long jugadorId, String message) {
		super("Error submiting bono for jugador ["+jugadorId+"] on week ["+weekId+"]. "+message);
	}
}
