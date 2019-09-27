package com.devteam.fantasy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devteam.fantasy.model.HistoryEvent;
import com.devteam.fantasy.model.User;

@Repository
public interface HistoryEventRepository extends JpaRepository<HistoryEvent, Long> {

	List<HistoryEvent> findAllByUserOrderByCreatedDate(User user);
}
