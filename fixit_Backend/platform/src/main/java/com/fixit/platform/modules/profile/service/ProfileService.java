package com.fixit.platform.modules.profile.service;

import com.fixit.platform.modules.profile.dto.ProfileResponse;
import com.fixit.platform.modules.profile.dto.UpdateProfileRequest;
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

    public ProfileResponse getMyProfile(UUID userId) {

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        ProfileResponse response = new ProfileResponse();

        response.setId(profile.getId());
        response.setFullName(profile.getFullName());
        response.setPhoneNumber(profile.getPhoneNumber());
        response.setLocation(profile.getLocation());
        response.setBio(profile.getBio());
        response.setProfileImageUrl(profile.getProfileImageUrl());
        response.setProviderProfileComplete(profile.isProviderProfileComplete());

        return response;
    }

    public ProfileResponse updateProfile(
            UUID userId,
            UpdateProfileRequest request
    ) {

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setLocation(request.getLocation());
        profile.setBio(request.getBio());
        profile.setProfileImageUrl(request.getProfileImageUrl());

        Profile updatedProfile = profileRepository.save(profile);

        ProfileResponse response = new ProfileResponse();

        response.setId(updatedProfile.getId());
        response.setFullName(updatedProfile.getFullName());
        response.setPhoneNumber(updatedProfile.getPhoneNumber());
        response.setLocation(updatedProfile.getLocation());
        response.setBio(updatedProfile.getBio());
        response.setProfileImageUrl(updatedProfile.getProfileImageUrl());
        response.setProviderProfileComplete(updatedProfile.isProviderProfileComplete());

        return response;
    }
}
