package com.devteam.fantasy.repository;


import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    List<User> findByOrderByIdAsc();

    User getByUsername(String username);

    User getById(Long id);

}