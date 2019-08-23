package com.devteam.fantasy.repository;


import com.devteam.fantasy.model.Role;
import com.devteam.fantasy.util.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
    boolean existsByName(RoleName roleName);
}