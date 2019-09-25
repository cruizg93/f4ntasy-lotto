package com.devteam.fantasy.exception;
import java.util.Formatter;

import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.SorteoDiaria;

public class CanNotInsertHistoricoBalanceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CanNotInsertHistoricoBalanceException() {
		super();
	}
	
	public CanNotInsertHistoricoBalanceException(Exception exception,Jugador jugador, SorteoDiaria sorteoDiaria) {
		super(String.format("Error creating history record for the balanace of jugador: [id:%s|Name:%s], on the Sorteo: [Type:%s|Time:%s]"
				,jugador.getId(),jugador.getName(),sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString(),sorteoDiaria.getSorteoTime())
				,exception);
	}

	public CanNotInsertHistoricoBalanceException(Exception e) {
		super(e);
	}
}
