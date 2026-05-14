package com.fixit.platform.modules.profile.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CompleteProviderProfileRequest {

    @NotBlank
    @Size(max = 1000)
    private String providerDescription;

    @NotNull
    @Min(0)
    @Max(60)
    private Integer experienceYears;

    @NotEmpty
    private List<UUID> skillIds;


}
