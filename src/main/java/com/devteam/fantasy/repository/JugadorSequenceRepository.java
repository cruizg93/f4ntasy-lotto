package com.devteam.fantasy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devteam.fantasy.model.JugadorSequence;

@Repository
public interface JugadorSequenceRepository extends JpaRepository<JugadorSequence, Long>{

	@Query(value = "SELECT last_value FROM jugadores_sequence", nativeQuery = true)
    Long getCurrentValue();
	
	@Query(value = "SELECT nextval('jugadores_sequence')", nativeQuery = true)
    Long getNextValue();
}
