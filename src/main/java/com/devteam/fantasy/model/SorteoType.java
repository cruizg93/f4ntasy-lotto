package com.devteam.fantasy.model;

import com.devteam.fantasy.util.SorteoTypeName;
import com.devteam.fantasy.util.StatusName;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
public class SorteoType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(length = 60)
    private SorteoTypeName sorteoTypeName;

    public SorteoType() {
    }

    public SorteoType(SorteoTypeName sorteoTypeName) {
        this.sorteoTypeName = sorteoTypeName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SorteoTypeName getSorteoTypeName() {
        return sorteoTypeName;
    }

    public void setSorteoTypeName(SorteoTypeName sorteoTypeName) {
        this.sorteoTypeName = sorteoTypeName;
    }
}
