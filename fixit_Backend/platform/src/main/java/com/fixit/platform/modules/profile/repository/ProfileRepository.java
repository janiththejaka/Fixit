package com.fixit.platform.modules.profile.repository;

import com.fixit.platform.modules.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile,Integer> {
    Optional<Profile> findByUserId(UUID userId);
    List<Profile> findByProviderProfileCompleteTrue();

}
