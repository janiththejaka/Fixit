package com.fixit.platform.modules.gig.service;

import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.UserRepository;
import com.fixit.platform.modules.gig.dto.CreateGigRequest;
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
}
