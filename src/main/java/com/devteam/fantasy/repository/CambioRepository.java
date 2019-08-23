package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Cambio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CambioRepository extends JpaRepository<Cambio, Long> {
    Cambio findFirstByOrderByIdDesc();
    Cambio getByCambio(Cambio cambio);
}
