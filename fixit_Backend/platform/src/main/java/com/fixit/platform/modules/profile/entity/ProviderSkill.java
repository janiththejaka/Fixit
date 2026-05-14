package com.fixit.platform.modules.profile.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "provider_skills")
public class ProviderSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;

}
