package com.fixit.platform.modules.profile.service;

import com.fixit.platform.modules.profile.dto.CompleteProviderProfileRequest;
import com.fixit.platform.modules.profile.dto.ProfileResponse;
import com.fixit.platform.modules.profile.dto.UpdateProfileRequest;
import com.fixit.platform.modules.profile.entity.Profile;
import com.fixit.platform.modules.profile.entity.ProviderSkill;
import com.fixit.platform.modules.profile.entity.Skill;
import com.fixit.platform.modules.profile.repository.ProfileRepository;
import com.fixit.platform.modules.profile.repository.ProviderSkillRepository;
import com.fixit.platform.modules.profile.repository.SkillRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final ProviderSkillRepository providerSkillRepository;


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

    public void completeProviderProfile(
            UUID userId,
            CompleteProviderProfileRequest request
    ) {

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // update provider fields
        profile.setProviderDescription(request.getProviderDescription());
        profile.setExperienceYears(request.getExperienceYears());

        // fetch skills
        List<Skill> skills = skillRepository.findByIdIn(request.getSkillIds());

        if (skills.size() != request.getSkillIds().size()) {
            throw new RuntimeException("Invalid skill selection");
        }

        // save provider skills
        for (Skill skill : skills) {

            ProviderSkill providerSkill = new ProviderSkill();

            providerSkill.setProfileId(profile.getId());
            providerSkill.setSkill(skill);

            providerSkillRepository.save(providerSkill);
        }

        // mark completed
        profile.setProviderProfileComplete(true);

        profileRepository.save(profile);
    }
}
