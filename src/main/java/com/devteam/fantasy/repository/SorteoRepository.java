package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Estado;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoType;
import com.devteam.fantasy.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public interface SorteoRepository extends CrudRepository<Sorteo, Long> {
    Sorteo getSorteoBySorteoTime(Timestamp timestamp);

    Sorteo getSorteoById(Long id);

    boolean existsSorteoBySorteoTime(Timestamp timestamp);

    List<Sorteo> findAllByStatus(Status status);

    @Query(value = "from Sorteo t where t.sorteoTime BETWEEN :startDate AND :endDate")
    List<Sorteo> getAllBetweenTimestamp(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    List<Sorteo> findTop60ByEstadoOrderByIdDesc(Estado estado);

    Sorteo getSorteoBySorteoTypeEquals(SorteoType sorteoType);



}
