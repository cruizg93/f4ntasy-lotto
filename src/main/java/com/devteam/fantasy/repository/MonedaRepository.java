package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Moneda;
import com.devteam.fantasy.util.MonedaName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonedaRepository extends JpaRepository<Moneda, Long> {
    boolean existsByMonedaName(MonedaName monedaName);
    Moneda findByMonedaName(MonedaName monedaName);

}
