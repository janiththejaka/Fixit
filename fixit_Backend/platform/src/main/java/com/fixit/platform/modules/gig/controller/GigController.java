package com.fixit.platform.modules.gig.controller;

import com.fixit.platform.common.response.ApiResponse;
import com.fixit.platform.modules.gig.dto.CreateGigRequest;
import com.fixit.platform.modules.gig.dto.GigCardResponse;
import com.fixit.platform.modules.gig.dto.ProviderGigResponse;
import com.fixit.platform.modules.gig.service.GigService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gigs")
public class GigController {

    private final GigService gigService;

    public GigController(GigService gigService) {
        this.gigService = gigService;
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
}