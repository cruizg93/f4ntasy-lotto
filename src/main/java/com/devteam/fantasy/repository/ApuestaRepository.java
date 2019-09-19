package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ApuestaRepository extends JpaRepository<Apuesta, Long> {
		
    Optional<Apuesta> findByNumero(Integer numero);
    Optional<Apuesta> findById(Long id);
    List<Apuesta> findAllByIdAndUser(Long id, User user);
    List<Apuesta> findAllByUser(User user);
    Apuesta getByNumeroAndAndId(Integer numero, Long id);
    Optional<Apuesta> findByNumeroAndSorteoDiaria(Integer numero, SorteoDiaria sorteoDiaria);
    Set<Apuesta> findAllBySorteoDiariaAndUser(SorteoDiaria sorteoDiaria, User user);
    List<Apuesta> findAllBySorteoDiariaAndUserOrderByNumeroAsc(SorteoDiaria sorteoDiaria, User user);
    Set<Apuesta> findAllBySorteoDiaria(SorteoDiaria sorteoDiaria);
    List<Apuesta> findAllBySorteoDiariaOrderByUserDesc(SorteoDiaria sorteoDiaria);
    Apuesta getApuestaByNumeroAndSorteoDiariaAndUser(Integer numero,SorteoDiaria sorteoDiaria, User user);
    List<Apuesta> findAllBySorteoDiariaAndNumeroAndUser(SorteoDiaria sorteoDiaria, Integer numero, User user);
    List<Apuesta> findAllBySorteoDiariaAndNumero(SorteoDiaria sorteoDiaria, Integer numero);
}
