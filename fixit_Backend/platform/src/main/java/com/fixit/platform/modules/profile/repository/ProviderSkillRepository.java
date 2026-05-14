package com.fixit.platform.modules.profile.repository;

import com.fixit.platform.modules.profile.entity.ProviderSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProviderSkillRepository extends JpaRepository<ProviderSkill, UUID> {
}
