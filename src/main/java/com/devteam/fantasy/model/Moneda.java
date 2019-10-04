package com.devteam.fantasy.model;


import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.RoleName;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "moneda")
public class Moneda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(length = 60)
    private MonedaName monedaName;

    public Moneda(){}

    public Moneda(MonedaName monedaName) {
        this.monedaName = monedaName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MonedaName getMonedaName() {
        return monedaName;
    }

    public void setMonedaName(MonedaName monedaName) {
        this.monedaName = monedaName;
    }

    @Override
    public String toString() {
        return monedaName.toString();
    }
}
