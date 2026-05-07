package com.fixit.platform.modules.auth.repository;

import com.fixit.platform.modules.auth.entity.AuthRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRoleRepository extends JpaRepository<AuthRole, Long> {
    Optional<AuthRole> findByName(String name);
    Optional<AuthRole> findById(Integer id);
}
