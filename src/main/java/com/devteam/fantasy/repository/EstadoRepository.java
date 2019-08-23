package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Estado;
import com.devteam.fantasy.util.EstadoName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoRepository extends JpaRepository<Estado, Long> {
    boolean existsByEstado(EstadoName estadoName);
    Estado getEstadoByEstado(EstadoName estadoName);
    Estado getEstadoById(Long id);

}
