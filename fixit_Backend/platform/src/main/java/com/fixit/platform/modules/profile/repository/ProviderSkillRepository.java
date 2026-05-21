package com.fixit.platform.modules.profile.repository;

import com.fixit.platform.modules.profile.entity.ProviderSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProviderSkillRepository extends JpaRepository<ProviderSkill, UUID> {
    List<ProviderSkill> findByProfileId(UUID profileId);
    boolean existsByProfileIdAndSkillId(
            UUID profileId,
            UUID skillId
    );
}
