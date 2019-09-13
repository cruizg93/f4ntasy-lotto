package com.devteam.fantasy.model;

import com.devteam.fantasy.util.EstadoName;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;


@Entity
@Table(name = "estados")
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(length = 60)
    private EstadoName estado;


    public Estado() {
    }

    public Estado(EstadoName estado) {
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoName getEstado() {
        return estado;
    }

    public void setEstado(EstadoName estado) {
        this.estado = estado;
    }

	@Override
	public String toString() {
		return "Estado [estado=" + estado + "]";
	}
    
    
}
