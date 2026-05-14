package com.fixit.platform.modules.profile.repository;

import com.fixit.platform.modules.profile.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    List<Skill> findByIdIn(List<UUID> ids);
}
