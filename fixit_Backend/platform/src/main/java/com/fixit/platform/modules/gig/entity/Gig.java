package com.fixit.platform.modules.gig.entity;

import com.fixit.platform.modules.profile.entity.Skill;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "gigs")
public class Gig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

}