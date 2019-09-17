package com.devteam.fantasy.exception;

public class CanNotInsertApuestaException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CanNotInsertApuestaException(String sorteoTitle, String numero, String message) {
		super("Error creating bet for the number ["+numero+"] for the sorteo: "+sorteoTitle+". "+message);
	}
}
