package com.fixit.platform.modules.profile.dto;

import com.fixit.platform.modules.gig.dto.GigCardResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProviderDetailResponse {
    private UUID profileId;

    private String fullName;

    private String location;

    private String providerDescription;

    private Integer experienceYears;

    private String profileImageUrl;

    private List<String> skills;

    private List<GigCardResponse> gigs;
}
