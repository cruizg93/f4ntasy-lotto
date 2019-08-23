package com.devteam.fantasy.model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

@Entity
@Table(name = "sorteos_diaria")
public class SorteoDiaria {

    @Id
    private Long id;

    @OneToOne
    @PrimaryKeyJoinColumn
    private Sorteo sorteo;

    @Column(name = "sorteo_time")
    private Timestamp sorteoTime;

    @OneToMany(mappedBy = "sorteoDiaria")
    private Set<Apuesta> apuestas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sorteo getSorteo() {
        return sorteo;
    }

    public void setSorteo(Sorteo sorteo) {
        this.sorteo = sorteo;
    }

    public Timestamp getSorteoTime() {
        return sorteoTime;
    }

    public void setSorteoTime(Timestamp sorteoTime) {
        this.sorteoTime = sorteoTime;
    }

    public Set<Apuesta> getApuestas() {
        return apuestas;
    }

    public void setApuestas(Set<Apuesta> apuestas) {
        this.apuestas = apuestas;
    }


}
