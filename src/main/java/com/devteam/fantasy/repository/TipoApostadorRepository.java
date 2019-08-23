package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.TipoApostador;
import com.devteam.fantasy.util.ApostadorName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoApostadorRepository extends JpaRepository<TipoApostador, Long> {
    boolean existsByApostadorName(ApostadorName apostadorName);
    TipoApostador findByApostadorName(ApostadorName apostadorName);
}
