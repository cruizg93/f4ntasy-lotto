package com.devteam.fantasy.model;

import com.devteam.fantasy.util.ApostadorName;
import com.devteam.fantasy.util.RoleName;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
public class TipoApostador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(length = 60)
    private ApostadorName apostadorName;

    public TipoApostador() {
    }

    public TipoApostador(ApostadorName apostadorName) {
        this.apostadorName = apostadorName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApostadorName getApostadorName() {
        return apostadorName;
    }

    public void setApostadorName(ApostadorName apostadorName) {
        this.apostadorName = apostadorName;
    }
}
