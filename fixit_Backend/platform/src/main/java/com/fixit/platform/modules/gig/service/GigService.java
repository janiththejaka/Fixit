package com.fixit.platform.modules.gig.service;

import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.UserRepository;
import com.fixit.platform.modules.gig.dto.CreateGigRequest;
import com.fixit.platform.modules.gig.dto.GigCardResponse;
import com.fixit.platform.modules.gig.dto.ProviderGigResponse;
import com.fixit.platform.modules.gig.dto.UpdateGigRequest;
import com.fixit.platform.modules.gig.entity.Gig;
import com.fixit.platform.modules.gig.repository.GigRepository;
import com.fixit.platform.modules.profile.entity.Profile;
import com.fixit.platform.modules.profile.entity.Skill;
import com.fixit.platform.modules.profile.repository.ProfileRepository;
import com.fixit.platform.modules.profile.repository.ProviderSkillRepository;
import com.fixit.platform.modules.profile.repository.SkillRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@AllArgsConstructor
public class GigService {

    private final GigRepository gigRepository;
    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final UserRepository authUserRepository;
    private final ProviderSkillRepository providerSkillRepository;

    public ApiResponse<String> createGig(
            Authentication authentication,
            CreateGigRequest request
    ) {

        String email = authentication.getName();

        User user = authUserRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Profile profile = profileRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Profile not found")
                );

        if (!profile.isProviderProfileComplete()) {
            throw new RuntimeException(
                    "Provider onboarding incomplete"
            );
        }


        boolean providerHasSkill =
                providerSkillRepository
                        .existsByProfileIdAndSkillId(
                                profile.getId(),
                                request.getSkillId()
                        );

        if (!providerHasSkill) {
            throw new RuntimeException(
                    "Skill not assigned to provider"
            );
        }
        Skill skill = skillRepository
                .findById(request.getSkillId())
                .orElseThrow(() ->
                        new RuntimeException("Skill not found")
                );

        Gig gig = new Gig();

        gig.setProfileId(profile.getId());
        gig.setSkill(skill);
        gig.setTitle(request.getTitle());
        gig.setDescription(request.getDescription());
        gig.setPrice(request.getPrice());

        gigRepository.save(gig);

        return new ApiResponse<>(
                true,
                "Gig created successfully",
                null
        );
    }

    public List<GigCardResponse> getPublicGigs() {

        return gigRepository.findPublicGigCards();

    }

    public List<ProviderGigResponse> getMyGigs(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = authUserRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Profile profile = profileRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Profile not found")
                );

        List<Gig> gigs =
                gigRepository.findByProfileId(profile.getId());

        return gigs.stream().map(gig -> {

            ProviderGigResponse response =
                    new ProviderGigResponse();

            response.setId(gig.getId());
            response.setTitle(gig.getTitle());
            response.setDescription(gig.getDescription());
            response.setPrice(gig.getPrice());
            response.setImageUrl(gig.getImageUrl());
            response.setActive(gig.isActive());

            response.setSkillName(
                    gig.getSkill().getName()
            );

            response.setCreatedAt(
                    gig.getCreatedAt()
            );

            return response;

        }).toList();
    }

    public void toggleGigStatus(
            UUID userId,
            UUID gigId
    ) {

        Profile profile = profileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Profile not found"
                        ));

        Gig gig = gigRepository
                .findByIdAndProfileId(
                        gigId,
                        profile.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Gig not found"
                        ));

        gig.setActive(!gig.isActive());

        gigRepository.save(gig);
    }

    public void updateGig(UUID userId, UUID gigId, UpdateGigRequest request){
        Profile profile = profileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        Gig gig = gigRepository
                .findByIdAndProfileId(
                        gigId,
                        profile.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Gig not found"));

        if (request.getSkillId() != null) {

            boolean providerHasSkill =
                    providerSkillRepository
                            .existsByProfileIdAndSkillId(
                                    profile.getId(),
                                    request.getSkillId()
                            );

            if (!providerHasSkill) {
                throw new RuntimeException(
                        "Skill not assigned to provider"
                );
            }

            Skill skill = skillRepository
                    .findById(request.getSkillId())
                    .orElseThrow(() ->
                            new RuntimeException("Skill not found"));

            gig.setSkill(skill);
        }

        if (request.getTitle() != null) {
            gig.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            gig.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            gig.setPrice(request.getPrice());
        }

        if (request.getImageUrl() != null) {
            gig.setImageUrl(request.getImageUrl());
        }

        gig.setUpdatedAt(LocalDateTime.now());

        gigRepository.save(gig);
    }

    public void deleteGig(UUID userId, UUID gigId) {
        Profile profile = profileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        Gig gig = gigRepository
                .findByIdAndProfileId(
                        gigId,
                        profile.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Gig not found"));

        gigRepository.delete(gig);
    }
}
