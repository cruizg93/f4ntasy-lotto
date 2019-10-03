package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Sorteo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    Jugador getById(Long id);
    List<Jugador> findAllByOrderByIdAsc();
//	Set<Jugador> findallByBalanceGreaterThan(double d);
//	Set<Jugador> findallByBalanceLessThan(double d);
	Set<Jugador> findAllByBalanceNot(double d);
	
	@Query("SELECt DISTINCT(j.id) From HistoricoApuestas ha JOIN ha.user j WHERE ha.sorteo = :sorteo")
	Set<Jugador> findAllWithHistoricoApuestasOnSorteo(Sorteo sorteo);
}
