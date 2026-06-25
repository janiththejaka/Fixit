package com.fixit.platform.modules.profile.repository;

import com.fixit.platform.modules.gig.entity.Gig;
import com.fixit.platform.modules.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile,Integer> {
    @Query("""
SELECT DISTINCT p
FROM Profile p
LEFT JOIN ProviderSkill ps
    ON ps.profileId = p.id
WHERE p.providerProfileComplete = true
AND (
    :location IS NULL
    OR LOWER(p.location)
       LIKE LOWER(CONCAT('%', :location, '%'))
)
AND (
    :skillId IS NULL
    OR ps.skill.id = :skillId
)
""")
    List<Profile> searchProviders(
            @Param("skillId") UUID skillId,
            @Param("location") String location
    );
    Optional<Profile> findByUserId(UUID userId);
    List<Profile> findByProviderProfileCompleteTrue();
    Optional<Profile>findById(UUID profileId);



}
