package com.fixit.platform.modules.gig.controller;

import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.auth.entity.User;
import com.fixit.platform.modules.auth.repository.UserRepository;
import com.fixit.platform.modules.gig.dto.CreateGigRequest;
import com.fixit.platform.modules.gig.dto.GigCardResponse;
import com.fixit.platform.modules.gig.dto.ProviderGigResponse;
import com.fixit.platform.modules.gig.dto.UpdateGigRequest;
import com.fixit.platform.modules.gig.service.GigService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gigs")
public class GigController {

    private final GigService gigService;
    private final UserRepository userRepository;

    public GigController(GigService gigService, UserRepository userRepository) {
        this.gigService = gigService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping
    public ApiResponse<String> createGig(
            Authentication authentication,
            @Valid @RequestBody CreateGigRequest request
    ) {

        return gigService.createGig(authentication, request);

    }

    @GetMapping
    public List<GigCardResponse> getPublicGigs() {

        return gigService.getPublicGigs();

    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/me")
    public List<ProviderGigResponse> getMyGigs(Authentication authentication)
    {

        return gigService.getMyGigs(authentication);

    }

    @PatchMapping("/{gigId}/toggle")
    @PreAuthorize("hasRole('PROVIDER')")
    public String toggleGigStatus(
            Authentication authentication,
            @PathVariable UUID gigId
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        gigService.toggleGigStatus(
                user.getId(),
                gigId
        );

        return "Gig status updated";
    }

    @PatchMapping("/update/{gigId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public String updateGig(
            Authentication authentication,
            @PathVariable UUID gigId,
            @Valid @RequestBody UpdateGigRequest request
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        gigService.updateGig(
                user.getId(),
                gigId,
                request
        );

        return "Gig updated successfully";
    }

    @DeleteMapping("/delete/{gigId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public String deleteGig(
            Authentication authentication,
            @PathVariable UUID gigId
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        gigService.deleteGig(
                user.getId(),
                gigId
        );

        return "Gig deleted successfully";
    }
}