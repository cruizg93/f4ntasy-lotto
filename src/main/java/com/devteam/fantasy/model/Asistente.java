package com.devteam.fantasy.model;

import javax.persistence.*;


@Entity
public class Asistente extends User {
    @OneToOne
    @JoinColumn(name = "jugador_id", referencedColumnName = "id")
    private Jugador jugador;

    public Asistente() {
    }

    public Asistente(String name, String username, String password, Jugador jugador) {
        super(name, username, password);
        this.jugador = jugador;
    }

    public Asistente(String name, String username, String password) {
        super(name, username, password);
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }
}
