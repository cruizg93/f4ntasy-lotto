package com.devteam.fantasy.exception;

import com.devteam.fantasy.model.Sorteo;

public class InvalidSorteoStateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InvalidSorteoStateException(Sorteo sorteo) {
		super("The sorteo with id:"+sorteo.getId()+" is not in a valid state to perform this action.");
	}

}
