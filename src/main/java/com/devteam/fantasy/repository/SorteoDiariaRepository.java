package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.util.EstadoName;

import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface SorteoDiariaRepository extends CrudRepository<SorteoDiaria, Long> {
    SorteoDiaria getSorteoActivoBySorteoTime(Timestamp timestamp);
    boolean existsSorteoActivoBySorteoTime(Timestamp timestamp);
    boolean existsSorteoDiariaBySorteo(Sorteo sorteo);
    Optional<SorteoDiaria> findById(Long id);
    Optional<SorteoDiaria> getSorteoDiariaById(Long id);
    List<SorteoDiaria> findAllBySorteoTimeLessThan(Timestamp timestamp);
    List<SorteoDiaria> findAllBySorteoEstadoEstadoNot(EstadoName estadoName);
	List<SorteoDiaria> findAllByOrderById();
	List<SorteoDiaria> findAllByOrderBySorteoTime();
}
