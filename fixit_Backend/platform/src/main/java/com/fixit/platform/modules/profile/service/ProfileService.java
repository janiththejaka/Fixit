package com.fixit.platform.modules.profile.service;

import com.fixit.platform.modules.profile.entity.Profile;
import com.fixit.platform.modules.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public void createBasicProfile(UUID userId, String fullName) {

        Profile profile = new Profile();

        profile.setUserId(userId);
        profile.setFullName(fullName);

        profileRepository.save(profile);
    }
}
