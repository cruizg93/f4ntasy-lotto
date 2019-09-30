package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Week;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WeekRepository extends JpaRepository<Week, Long> {
	List<Week> findAllByOrderByIdDesc();
}
