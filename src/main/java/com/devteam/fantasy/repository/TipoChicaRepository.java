package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.TipoChica;
import com.devteam.fantasy.util.ChicaName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoChicaRepository extends JpaRepository<TipoChica, Long> {

    boolean existsByChicaName(ChicaName chicaName);
    TipoChica findByChicaName(ChicaName chicaName);
}
