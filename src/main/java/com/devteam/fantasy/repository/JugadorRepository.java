package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.UserState;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    Jugador getById(Long id);
    Jugador findFirstByOrderByIdDesc();
    List<Jugador> findAllByUserStateOrderByIdAsc(UserState userState);
//	Set<Jugador> findallByBalanceGreaterThan(double d);
//	Set<Jugador> findallByBalanceLessThan(double d);
	Set<Jugador> findAllByBalanceNot(double d);
	
	@Query("SELECt DISTINCT(j) From HistoricoApuestas ha JOIN ha.user j WHERE ha.sorteo = :sorteo")
	Set<Jugador> findAllWithHistoricoApuestasOnSorteo(Sorteo sorteo);
	List<Jugador> findAllByOrderByIdAsc();
}
