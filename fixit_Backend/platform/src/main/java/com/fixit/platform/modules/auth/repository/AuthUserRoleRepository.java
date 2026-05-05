package com.fixit.platform.modules.auth.repository;

import com.fixit.platform.modules.auth.entity.AuthRole;
import com.fixit.platform.modules.auth.entity.AuthUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRoleRepository extends JpaRepository<AuthUserRole, Long> {
}
