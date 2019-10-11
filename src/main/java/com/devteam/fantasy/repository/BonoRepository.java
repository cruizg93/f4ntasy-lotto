package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Bono;
import com.devteam.fantasy.model.Cambio;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.model.Week;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BonoRepository extends JpaRepository<Bono, Long> {
	
	List<Bono> findAllByWeek(Week week);
	List<Bono> findAllByUser(User user);
	List<Bono> findAllByWeekAndUser(Week week, User user);
}
