package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.NumeroGanador;
import com.devteam.fantasy.model.Sorteo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NumeroGanadorRepository extends JpaRepository<NumeroGanador, Long> {
    List<NumeroGanador> findTop30ByOrderByIdDesc();
    NumeroGanador getBySorteo(Sorteo sorteo);
    Optional<Integer> getByNumeroGanador(Integer numero);
    Optional<NumeroGanador> getNumeroGanadorBySorteo(Sorteo sorteo);
	List<NumeroGanador> findAllByOrderBySorteoSorteoTime();
	List<NumeroGanador> findAllByOrderBySorteoSorteoTimeDesc();
	List<NumeroGanador> findTop90ByOrderBySorteoSorteoTimeDesc();
}
