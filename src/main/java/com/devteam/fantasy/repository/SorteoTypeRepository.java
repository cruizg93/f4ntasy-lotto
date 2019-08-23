package com.devteam.fantasy.repository;

import com.devteam.fantasy.model.SorteoType;
import com.devteam.fantasy.util.SorteoTypeName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SorteoTypeRepository extends JpaRepository<SorteoType, Long> {
    boolean existsBySorteoTypeName(SorteoTypeName sorteoTypeName);
    SorteoType getBySorteoTypeName(SorteoTypeName sorteoTypeName);

}
