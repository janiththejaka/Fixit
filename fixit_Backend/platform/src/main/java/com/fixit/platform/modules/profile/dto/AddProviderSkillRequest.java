package com.fixit.platform.modules.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class AddProviderSkillRequest {
    @NotNull(message = "Skill is required")
    private UUID skillId;
}
