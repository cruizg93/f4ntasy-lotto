package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.HistoricoBalance;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Week;
import com.devteam.fantasy.util.BalanceType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface HistoricoBalanceRepository extends JpaRepository<HistoricoBalance, Long> {
    List<HistoricoBalance> findAllBySorteoTime(Timestamp sorteoTime);
    HistoricoBalance findBySorteoTimeAndJugador(Timestamp sorteoTime, Jugador jugador);
    Optional<HistoricoBalance> findByBalanceTypeAndJugadorAndWeek(BalanceType balanceType, Jugador jugador, Week week);
	List<HistoricoBalance> findallByWeekAndJugadorAndBalanceTypeOrderBySorteoTimeAsc(Week week, Jugador jugador,BalanceType daily);
}
