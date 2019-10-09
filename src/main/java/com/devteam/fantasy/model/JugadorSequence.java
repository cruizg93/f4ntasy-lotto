package com.devteam.fantasy.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class JugadorSequence {

    @Id
    @GenericGenerator(
        name = "jugadores_sequence",
        strategy = "sequence",
        parameters = {
            @org.hibernate.annotations.Parameter(
                name = "jugadores_sequence",
                value = "sequence"
            )
 
    })
    @GeneratedValue(generator = "jugadores_sequence")
    private Long id;

}