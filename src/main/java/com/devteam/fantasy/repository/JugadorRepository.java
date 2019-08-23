package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    Jugador getById(Long id);
    List<Jugador> findAllByOrderByIdAsc();

}
