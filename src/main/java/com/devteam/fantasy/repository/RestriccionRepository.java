package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Restriccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface RestriccionRepository extends JpaRepository<Restriccion, Long> {
    List<Restriccion> findAllByTimestamp(Timestamp timestamp);
    boolean existsByNumero(Integer numero);
    Restriccion findByNumero(Integer numero);
    Restriccion getByNumeroAndTimestamp(Integer numero, Timestamp timestamp);
}
