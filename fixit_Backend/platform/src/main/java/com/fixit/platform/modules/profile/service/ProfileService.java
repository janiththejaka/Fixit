package com.fixit.platform.modules.profile.service;

import com.fixit.platform.modules.profile.dto.*;
import com.fixit.platform.modules.profile.entity.Profile;
import com.fixit.platform.modules.profile.entity.ProviderSkill;
import com.fixit.platform.modules.profile.entity.Skill;
import com.fixit.platform.modules.profile.repository.ProfileRepository;
import com.fixit.platform.modules.profile.repository.ProviderSkillRepository;
import com.fixit.platform.modules.profile.repository.SkillRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final ProviderSkillRepository providerSkillRepository;


    public Profile createBasicProfile(UUID userId, String fullName) {

        Profile profile = new Profile();

        profile.setUserId(userId);
        profile.setFullName(fullName);

        return profileRepository.save(profile);
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


    //helper method for provider completion check
    private boolean isProviderProfileComplete(
            Profile profile
    ) {

        boolean hasSkill = !providerSkillRepository.findByProfileId(profile.getId()).isEmpty();

        return profile.getFullName() != null
                && !profile.getFullName().isBlank()
                && profile.getPhoneNumber() != null
                && !profile.getPhoneNumber().isBlank()
                && profile.getLocation() != null
                && !profile.getLocation().isBlank()
                && profile.getProviderDescription() != null
                && !profile.getProviderDescription().isBlank()
                && profile.getExperienceYears() != null
                && hasSkill;
    }


    public void completeProviderProfile(
            UUID userId,
            CompleteProviderProfileRequest request
    ) {

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // update provider fields
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setLocation(request.getLocation());
        profile.setProviderDescription(request.getProviderDescription());
        profile.setExperienceYears(request.getExperienceYears());

        // mark completed
        profile.setProviderProfileComplete(isProviderProfileComplete(profile));

        profileRepository.save(profile);
    }

    public List<SkillResponse> getAllSkills() {

        List<Skill> skills = skillRepository.findAll();

        return skills.stream().map(skill -> {

            SkillResponse response = new SkillResponse();

            response.setId(skill.getId());
            response.setName(skill.getName());
            response.setSlug(skill.getSlug());

            return response;

        }).toList();
    }


    public List<ProviderCardResponse> getProviders() {

        List<Profile> providers =
                profileRepository.findByProviderProfileCompleteTrue();

        return providers.stream().map(profile -> {

            ProviderCardResponse response =
                    new ProviderCardResponse();

            response.setProfileId(profile.getId());
            response.setFullName(profile.getFullName());
            response.setLocation(profile.getLocation());
            response.setProviderDescription(
                    profile.getProviderDescription()
            );
            response.setExperienceYears(
                    profile.getExperienceYears()
            );

            List<String> skillNames =
                    providerSkillRepository
                            .findByProfileId(profile.getId())
                            .stream()
                            .map(ps -> ps.getSkill().getName())
                            .toList();

            response.setSkills(skillNames);

            return response;

        }).toList();
    }
}
