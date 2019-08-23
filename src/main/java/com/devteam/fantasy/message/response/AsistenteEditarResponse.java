package com.devteam.fantasy.message.response;

public class AsistenteEditarResponse {
    private Long id;
    private String username;
    private String name;
    private String jugadorUsername;
    private String jugadorName;
    private String jugadorMoneda;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJugadorUsername() {
        return jugadorUsername;
    }

    public void setJugadorUsername(String jugadorUsername) {
        this.jugadorUsername = jugadorUsername;
    }

    public String getJugadorName() {
        return jugadorName;
    }

    public void setJugadorName(String jugadorName) {
        this.jugadorName = jugadorName;
    }

    public String getJugadorMoneda() {
        return jugadorMoneda;
    }

    public void setJugadorMoneda(String jugadorMoneda) {
        this.jugadorMoneda = jugadorMoneda;
    }
}
