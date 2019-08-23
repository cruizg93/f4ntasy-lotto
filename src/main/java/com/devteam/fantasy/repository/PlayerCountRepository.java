package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.PlayerCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerCountRepository extends JpaRepository<PlayerCount, Long> {
}
