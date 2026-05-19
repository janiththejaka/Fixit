package com.fixit.platform.modules.profile.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter

public class ProviderCardResponse {

    private UUID profileId;

    private String fullName;

    private String location;

    private String providerDescription;

    private Integer experienceYears;

    private List<String> skills;

}
