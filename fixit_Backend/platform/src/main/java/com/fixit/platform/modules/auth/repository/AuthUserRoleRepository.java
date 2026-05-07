package com.fixit.platform.modules.auth.repository;

import com.fixit.platform.modules.auth.entity.AuthRole;
import com.fixit.platform.modules.auth.entity.AuthUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuthUserRoleRepository extends JpaRepository<AuthUserRole, Long> {
    List<AuthUserRole> findByUserId(UUID userId);
}
