package com.devteam.fantasy.exception;

public class CanNotRemoveApuestaException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CanNotRemoveApuestaException(String sorteoTitle, String message) {
		super("Error deleting all bets for for the sorteo: "+sorteoTitle+". "+message);
	}
	
	public CanNotRemoveApuestaException(String sorteoTitle, String numero, String message) {
		super("Error deleting bet for the number ["+numero+"] for the sorteo: "+sorteoTitle+". "+message);
	}
}
