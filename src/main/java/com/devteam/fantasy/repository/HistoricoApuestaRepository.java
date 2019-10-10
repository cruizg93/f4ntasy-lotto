package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface HistoricoApuestaRepository extends JpaRepository<HistoricoApuestas, Long> {
	List<HistoricoApuestas> findAllBySorteo(Sorteo sorteo);
	List<HistoricoApuestas> findAllBySorteoAndUser(Sorteo sorteo, User user);
	List<HistoricoApuestas> findAllBySorteoAndUserOrderByNumeroAsc(Sorteo sorteo, User user);
	
	List<HistoricoApuestas> findAllBySorteoAndNumero(Sorteo sorteo, Integer numero);
	List<HistoricoApuestas> findAllBySorteoAndUserAndAsistenteOrderByNumeroAsc(Sorteo sorteo, User user, Object object);
	List<HistoricoApuestas> findAllBySorteoSorteoTimeBetween(Timestamp monday, Timestamp sunday);
	List<HistoricoApuestas> findAllBySorteoAndAsistente(Sorteo sorteo, User user);
	List<HistoricoApuestas> findAllByAsistente(User user);
	List<HistoricoApuestas> findAllByUser(User user);
}
