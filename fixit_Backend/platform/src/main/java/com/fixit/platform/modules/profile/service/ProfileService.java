package com.fixit.platform.modules.profile.service;

import com.fixit.platform.modules.profile.dto.*;
import com.fixit.platform.modules.profile.entity.Profile;
import com.fixit.platform.modules.profile.entity.ProviderSkill;
import com.fixit.platform.modules.profile.entity.Skill;
import com.fixit.platform.modules.profile.repository.ProfileRepository;
import com.fixit.platform.modules.profile.repository.ProviderSkillRepository;
import com.fixit.platform.modules.profile.repository.SkillRepository;
import com.fixit.platform.modules.gig.dto.GigCardResponse;
import com.fixit.platform.modules.gig.repository.GigRepository;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final ProviderSkillRepository providerSkillRepository;
    private final GigRepository gigRepository;


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

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }

        if (
                request.getFullName() == null &&
                        request.getPhoneNumber() == null &&
                        request.getLocation() == null &&
                        request.getBio() == null &&
                        request.getProfileImageUrl() == null
        ) {
            throw new RuntimeException("No fields provided for update");
        }

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

    // Additional skill adding and deleting logics

    public void addProviderSkill(
            UUID userId,
            UUID skillId
    ){
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new RuntimeException("Skill not found"));

        boolean alreadyAssigned =
                providerSkillRepository
                        .existsByProfileIdAndSkillId(
                                profile.getId(),
                                skillId
                        );

        if (alreadyAssigned) {
            throw new RuntimeException(
                    "Skill already assigned"
            );
        }

        ProviderSkill providerSkill =
                new ProviderSkill();

        providerSkill.setProfileId(profile.getId());
        providerSkill.setSkill(skill);

        providerSkillRepository.save(providerSkill);
    }

    public List<ProviderSkillResponse> getProviderSkills(UUID userId)
    {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        return providerSkillRepository
                .findByProfileId(profile.getId())
                .stream()
                .map(ps -> new ProviderSkillResponse(
                        ps.getSkill().getId(),
                        ps.getSkill().getName(),
                        ps.getSkill().getSlug()
                ))
                .toList();
    }

    public void removeProviderSkill(UUID userId, UUID skillId)
    {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        List<ProviderSkill> skills =
                providerSkillRepository.findByProfileId(
                        profile.getId()
                );

        if (skills.size() <= 1) {
            throw new RuntimeException(
                    "Provider must have at least one skill"
            );
        }

        boolean exists =
                providerSkillRepository
                        .existsByProfileIdAndSkillId(
                                profile.getId(),
                                skillId
                        );

        if (!exists) {
            throw new RuntimeException(
                    "Skill not assigned to provider"
            );
        }

        providerSkillRepository
                .deleteByProfileIdAndSkillId(
                        profile.getId(),
                        skillId
                );
    }

    public ProviderDetailResponse getProviderDetails(
            UUID profileId
    ){
        Profile profile = profileRepository
                .findById(profileId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Provider not found"
                        ));

        if (!profile.isProviderProfileComplete()) {
            throw new RuntimeException(
                    "Provider not available"
            );
        }

        List<String> skills =
                providerSkillRepository
                        .findByProfileId(profileId)
                        .stream()
                        .map(ps -> ps.getSkill().getName())
                        .toList();

        List<GigCardResponse> gigs =
                gigRepository
                        .findByProfileIdAndActiveTrue(
                                profileId
                        )
                        .stream()
                        .map(gig -> {

                            GigCardResponse response =
                                    new GigCardResponse();

                            response.setGigId(gig.getId());
                            response.setTitle(gig.getTitle());
                            response.setDescription(
                                    gig.getDescription()
                            );
                            response.setPrice(gig.getPrice());
                            response.setImageUrl(
                                    gig.getImageUrl()
                            );

                            return response;

                        })
                        .toList();

        ProviderDetailResponse response = new ProviderDetailResponse();

        response.setProfileId(profile.getId());
        response.setFullName(profile.getFullName());
        response.setLocation(profile.getLocation());
        response.setProviderDescription(profile.getProviderDescription());
        response.setExperienceYears(
                profile.getExperienceYears()
        );
        response.setProfileImageUrl(
                profile.getProfileImageUrl()
        );

        response.setSkills(skills);
        response.setGigs(gigs);

        return response;
    }

    public List<ProviderCardResponse> searchProviders(
            UUID skillId,
            String location
    ){List<Profile> providers = profileRepository.searchProviders(skillId, location);

        return providers.stream().map(profile -> {

            ProviderCardResponse response = new ProviderCardResponse();

            response.setProfileId(profile.getId());
            response.setFullName(profile.getFullName());
            response.setLocation(profile.getLocation());
            response.setProviderDescription(profile.getProviderDescription());
            response.setExperienceYears(profile.getExperienceYears());

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
