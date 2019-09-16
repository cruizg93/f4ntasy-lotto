package com.devteam.fantasy.model;

import javax.persistence.*;

import com.devteam.fantasy.util.SorteoTypeName;

import java.sql.Timestamp;


@Entity
@Table(name = "sorteos")
public class Sorteo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sorteo_time")
    private Timestamp sorteoTime;


    @ManyToOne
    @JoinColumn
    private Estado estado;

    @ManyToOne
    @JoinColumn
    private Status status;

    @ManyToOne
    @JoinColumn
    private SorteoType sorteoType;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getSorteoTime() {
        return sorteoTime;
    }

    public void setSorteoTime(Timestamp sorteoTime) {
        this.sorteoTime = sorteoTime;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public SorteoType getSorteoType() {
        return sorteoType;
    }

    public void setSorteoType(SorteoType sorteoType) {
        this.sorteoType = sorteoType;
    }
    
	@Override
	public String toString() {
		return "Sorteo [id=" + id + ", sorteoTime=" + sorteoTime + ", estado=" + estado + ", status=" + status
				+ ", sorteoType=" + sorteoType + "]";
	}
    
    
}
