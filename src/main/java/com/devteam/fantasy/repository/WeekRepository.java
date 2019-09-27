package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Week;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WeekRepository extends JpaRepository<Week, Long> {
}
