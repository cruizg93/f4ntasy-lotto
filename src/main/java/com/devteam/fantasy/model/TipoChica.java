package com.devteam.fantasy.model;

import com.devteam.fantasy.util.ChicaName;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "tipo_chica")
public class TipoChica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(length = 60)
    private ChicaName chicaName;


    public TipoChica() {
    }

    public TipoChica(ChicaName chicaName) {
        this.chicaName = chicaName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChicaName getChicaName() {
        return chicaName;
    }

    public void setChicaName(ChicaName chicaName) {
        this.chicaName = chicaName;
    }

	@Override
	public String toString() {
		return "TipoChica [chicaName=" + chicaName.toString() + "]";
	}
    
    
}
