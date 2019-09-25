package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.HistoricoBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface HistoricoBalanceRepository extends JpaRepository<HistoricoBalance, Long> {
    List<HistoricoBalance> findAllBySorteoTime(Timestamp sorteoTime);
}
