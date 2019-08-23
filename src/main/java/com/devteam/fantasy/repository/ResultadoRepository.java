package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Resultado;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {
    List<Resultado> findAllBySorteo(Sorteo sorteo);
    List<Resultado> findAllBySorteoAndUser(Sorteo sorteo, User user);
    Resultado getBySorteoAndUser(Sorteo sorteo, User user);
}
