package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Status;
import com.devteam.fantasy.model.TipoChica;
import com.devteam.fantasy.util.ChicaName;
import com.devteam.fantasy.util.StatusName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {

    boolean existsByStatus(StatusName status);
    Status getByStatus(StatusName status);
}
