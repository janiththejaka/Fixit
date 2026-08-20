package com.fixit.platform.modules.profile.controller;

import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.UserRepository;
import com.fixit.platform.modules.profile.dto.*;
import com.fixit.platform.modules.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Profiles",
        description = "User profiles, provider onboarding and provider discovery"
)

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository userRepository;


    @GetMapping("/me")
    public ProfileResponse getMyProfile(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return profileService.getMyProfile(user.getId());
    }

    @PatchMapping("/me")
    public ProfileResponse updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return profileService.updateProfile(user.getId(), request);
    }

    @PostMapping("/provider/complete")
    @PreAuthorize("hasRole('PROVIDER')")
    public String completeProviderProfile(
            Authentication authentication,
            @Valid @RequestBody CompleteProviderProfileRequest request
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        profileService.completeProviderProfile(user.getId(), request);

        return "Provider profile completed";
    }

    @GetMapping("/providers/{profileId}")
    public ProviderDetailResponse getProviderDetails(
            @PathVariable UUID profileId
    ) {
        return profileService
                .getProviderDetails(profileId);
    }


    @GetMapping("/providers")
    public List<ProviderCardResponse> getProviders() {
        return profileService.getProviders();
    }

    @GetMapping("/providers/search")
    public List<ProviderCardResponse> searchProviders(
            @RequestParam(required = false) UUID skillId,
            @RequestParam(required = false) String location
    ) {

        return profileService.searchProviders(
                skillId,
                location
        );
    }
}


