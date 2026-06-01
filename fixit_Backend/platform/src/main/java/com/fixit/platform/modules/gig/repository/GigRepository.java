package com.fixit.platform.modules.gig.repository;

import com.fixit.platform.modules.gig.entity.Gig;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fixit.platform.modules.gig.dto.GigCardResponse;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GigRepository extends JpaRepository<Gig, UUID> {

    List<Gig> findByProfileId(UUID profileId);
    @Query("""
        SELECT new com.fixit.platform.modules.gig.dto.GigCardResponse(
            g.id,
            g.title,
            g.description,
            g.price,
            g.imageUrl,
            p.fullName,
            p.location,
            s.name
        )
        FROM Gig g
        JOIN Profile p ON p.id = g.profileId
        JOIN Skill s ON s.id = g.skill.id
        WHERE g.active = true
    """)
    List<GigCardResponse> findPublicGigCards();

    Optional<Gig> findByIdAndProfileId(UUID id, UUID profileId);
}
