package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.UserState;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenteRepository extends JpaRepository<Asistente, Long> {
    List<Asistente> findAllByJugadorAndUserState(Jugador jugador, UserState userState);

    List<Asistente> findAllByJugador(Jugador jugador);

	Asistente findFirstByJugadorOrderByIdDesc(Jugador jugador);


}
 